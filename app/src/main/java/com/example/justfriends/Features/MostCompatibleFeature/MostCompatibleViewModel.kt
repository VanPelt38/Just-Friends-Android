package com.example.justfriends.Features.MostCompatibleFeature

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.DataModels.Compatible
import com.example.justfriends.DataModels.ExpiringMatch
import com.example.justfriends.DataModels.MatchModel
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*


class MostCompatibleViewModel(
    private val justFriends: Application,
    private val dataStoreManager: DataStoreManager,
    private val currentView: MutableState<String>,
    private val navBarTitle: MutableState<String>,
    private val shouldShowFAB: MutableState<Boolean>
) : AndroidViewModel(justFriends) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    var isLoading = mutableStateOf(false)
    var compatibleUsers = mutableStateListOf<Compatible>()
    var userInterests = listOf<String>()

    val errorAlertState = mutableStateOf<String?>(null)
    val errorAlertStateTitle = mutableStateOf<String?>(null)
    val errorAlertStateAccept = mutableStateOf<String?>(null)
    val errorAlertStateDecline = mutableStateOf<String?>(null)
    val snackBarMessage = mutableStateOf<String?>(null)

    var selectedFriendIndex: Int? = null
    private var matchStatusesArray = listOf<MatchModel>()
    private var expiringMatchesArray = listOf<ExpiringMatch>()
    private var blockedUserIDs = listOf<String>()
    private var fcmToken: String? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private data class Candidate(
        val name: String,
        val age: String,
        val gender: String,
        val picture: String?,
        val userID: String,
        val town: String?,
        val occupation: String?,
        val summary: String?,
        val interests: List<String>,
        val distanceKm: Double,
        val sharedCount: Int
    )

    fun setScaffold() {
        currentView.value = ""
        navBarTitle.value = "Most Compatible"
        shouldShowFAB.value = true
    }

    fun loadUserInterests() {
        viewModelScope.launch {
            val userID = auth.currentUser?.uid ?: return@launch
            val docs = db.collection("users")
                .document(userID)
                .collection("profile")
                .whereEqualTo("userID", userID)
                .limit(1)
                .get()
                .await()
            if (!docs.isEmpty) {
                val data = docs.documents[0].data ?: return@launch
                userInterests = (data["interests"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            }
        }
    }

    fun calculate() {
        isLoading.value = true
        viewModelScope.launch {
            if (!canCalculate()) {
                errorAlertStateTitle.value = "Too soon!"
                errorAlertState.value = "You can only recalculate once every 24 hours."
                errorAlertStateAccept.value = "OK"
                isLoading.value = false
                return@launch
            }

            if (userInterests.size < 5) {
                errorAlertStateTitle.value = "More interests needed"
                errorAlertState.value = "Add at least 5 interests to your profile to use this feature."
                errorAlertStateAccept.value = "OK"
                isLoading.value = false
                return@launch
            }

            val location = getLastKnownLocation()
            if (location == null) {
                errorAlertStateTitle.value = "Location required"
                errorAlertState.value = "Please enable location services to find compatible people near you."
                errorAlertStateAccept.value = "OK"
                isLoading.value = false
                return@launch
            }

            try {
                val userID = auth.currentUser?.uid ?: run { isLoading.value = false; return@launch }

                loadBlockedAndMatchData(userID)
                val excludedIDs = blockedUserIDs.toHashSet().apply {
                    addAll(expiringMatchesArray.map { it.userID })
                }

                // Load location data from statuses keyed by userID
                val statusDocs = db.collection("statuses")
                    .whereNotEqualTo("userID", userID)
                    .get()
                    .await()
                val locationMap = mutableMapOf<String, Pair<Double, Double>>()
                for (doc in statusDocs.documents) {
                    val data = doc.data ?: continue
                    val uid = data["userID"] as? String ?: continue
                    val lat = data["latitude"] as? Double ?: continue
                    val lng = data["longitude"] as? Double ?: continue
                    locationMap[uid] = Pair(lat, lng)
                }

                // Load all user documents
                val allUsers = db.collection("users").get().await()

                // Buckets indexed by shared interest count (0..5+)
                val buckets = Array(6) { mutableListOf<Candidate>() }

                for (userDoc in allUsers.documents) {
                    val otherUserID = userDoc.id
                    if (otherUserID == userID) continue
                    if (excludedIDs.contains(otherUserID)) continue

                    val profileDocs = db.collection("users")
                        .document(otherUserID)
                        .collection("profile")
                        .limit(1)
                        .get()
                        .await()

                    if (profileDocs.isEmpty) continue
                    val data = profileDocs.documents[0].data ?: continue

                    val interests = (data["interests"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val sharedCount = interests.count { it in userInterests }

                    val otherLoc = locationMap[otherUserID]
                    val distanceKm = if (otherLoc != null) {
                        haversineKm(location.first, location.second, otherLoc.first, otherLoc.second)
                    } else {
                        999.0
                    }

                    buckets[minOf(sharedCount, 5)].add(
                        Candidate(
                            name = data["name"] as? String ?: "",
                            age = (data["age"] as? Number ?: 0).toString(),
                            gender = data["gender"] as? String ?: "",
                            picture = data["picture"] as? String,
                            userID = otherUserID,
                            town = data["town"] as? String,
                            occupation = data["occupation"] as? String,
                            summary = data["summary"] as? String,
                            interests = interests,
                            distanceKm = distanceKm,
                            sharedCount = sharedCount
                        )
                    )
                }

                // Assemble top 5: highest shared count first, sorted by distance within each bucket
                val result = mutableListOf<Candidate>()
                for (i in 5 downTo 0) {
                    buckets[i].sortBy { it.distanceKm }
                    for (candidate in buckets[i]) {
                        if (result.size == 5) break
                        result.add(candidate)
                    }
                    if (result.size == 5) break
                }

                compatibleUsers.clear()
                for (c in result) {
                    compatibleUsers.add(
                        Compatible(
                            name = c.name,
                            age = c.age,
                            gender = c.gender,
                            picture = c.picture,
                            userID = c.userID,
                            town = c.town,
                            occupation = c.occupation,
                            summary = c.summary,
                            interests = c.interests,
                            distanceAway = maxOf(1, c.distanceKm.toInt())
                        )
                    )
                }

                dataStoreManager.write(DataStoreKeys.lastCompatibleCalculation, dateFormat.format(Date()))

            } catch (e: Exception) {
                errorAlertStateTitle.value = "Uh-oh"
                errorAlertState.value = "Could not calculate compatibility. Please try again.\n\nError: ${e.message}"
                errorAlertStateAccept.value = "OK"
            }

            isLoading.value = false
        }
    }

    fun seeFriendProfileForIndex(index: Int) {
        viewModelScope.launch {
            dataStoreManager.write(DataStoreKeys.friendIDForProfile, compatibleUsers[index].userID)
            _navigateTo.value = View.friendProfile.name
        }
    }

    private suspend fun loadBlockedAndMatchData(userID: String) {
        val blockedUsersDocs = db.collection("users")
            .document(userID)
            .collection("blockedUsers")
            .get()
            .await()
        blockedUserIDs = blockedUsersDocs.documents.mapNotNull { it.data?.get("blockedUserID") as? String }

        val expiringDocs = db.collection("users")
            .document(userID)
            .collection("expiringRequests")
            .get()
            .await()
        expiringMatchesArray = expiringDocs.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            ExpiringMatch(
                ownUserID = data["ownUserID"] as? String ?: return@mapNotNull null,
                timeStamp = (data["timeStamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                userID = data["userID"] as? String ?: return@mapNotNull null
            )
        }

        val matchStatusDocs = db.collection("users")
            .document(userID)
            .collection("matchStatuses")
            .get()
            .await()
        matchStatusesArray = matchStatusDocs.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            MatchModel(
                activity = data["activity"] as? String ?: "error",
                time = data["time"] as? String ?: "error",
                ownUserID = data["ownUserID"] as? String ?: "error",
                age = (data["age"] as? Number ?: 0).toString(),
                gender = data["gender"] as? String ?: "error",
                accepted = data["accepted"] as? Boolean ?: false,
                fcmToken = data["fcmToken"] as? String ?: "error",
                name = data["name"] as? String ?: "error",
                distanceAway = data["distanceAway"] as? Int ?: 0,
                chatID = data["chatID"] as? String ?: "error",
                picture = data["imageURL"] as? String ?: "error",
                ID = data["ID"] as? String ?: "error"
            )
        }
    }

    fun startMatching() {
        if (alreadyMatched()) {
            errorAlertStateTitle.value = "Uh-oh"
            errorAlertState.value = "Looks like the two of you are already connected. Try sending them a message instead."
            errorAlertStateAccept.value = "Okay"
        } else {
            viewModelScope.launch {
                matchWithUser()
            }
        }
    }

    private fun alreadyMatched(): Boolean {
        val index = selectedFriendIndex ?: return false
        val candidate = compatibleUsers.getOrNull(index) ?: return false
        return matchStatusesArray.any { it.ID == candidate.userID }
    }

    private suspend fun matchWithUser() {
        val index = selectedFriendIndex ?: return
        val candidate = compatibleUsers.getOrNull(index) ?: return
        val userID = auth.currentUser?.uid ?: return
        val myProfileDoc = db.collection("users")
            .document(userID)
            .collection("profile")
            .whereEqualTo("userID", userID)
            .limit(1)
            .get()
            .await()
        val myProfile = myProfileDoc.documents.firstOrNull()?.data ?: return
        if (fcmToken == null) {
            fcmToken = try { FirebaseMessaging.getInstance().token.await() } catch (e: Exception) { null }
        }

        val daterID = candidate.userID

        try {
            val daterDoc = db.collection("users").document(daterID).get().await()
            if (!daterDoc.exists()) {
                errorAlertStateTitle.value = "Uh-oh"
                errorAlertState.value = "Unfortunately this user is no longer available."
                errorAlertStateAccept.value = "OK"
                return
            }

            val expiringRequestData = mapOf(
                "timeStamp" to Date(),
                "userID" to daterID,
                "ownUserID" to userID
            )
            db.collection("users")
                .document(userID)
                .collection("expiringRequests")
                .document(daterID)
                .set(expiringRequestData)
                .await()

            val matchStatusData = mapOf(
                "name" to (myProfile["name"] as? String ?: ""),
                "imageURL" to (myProfile["picture"] as? String ?: ""),
                "activity" to "none",
                "time" to "none",
                "ID" to userID,
                "age" to (myProfile["age"] as? Number ?: 0).toInt(),
                "gender" to (myProfile["gender"] as? String ?: ""),
                "accepted" to false,
                "fcmToken" to fcmToken,
                "realmID" to "androidUser",
                "ownUserID" to daterID,
                "chatID" to "none",
                "distanceAway" to candidate.distanceAway
            )
            db.collection("users")
                .document(daterID)
                .collection("matchStatuses")
                .document(userID)
                .set(matchStatusData)
                .await()

            val existingNotifications = db.collection("users")
                .document(daterID)
                .collection("matchNotifications")
                .get()
                .await()
            val notificationAlreadyExists = existingNotifications.documents.any {
                (it.data?.get("suitorID") as? String) == userID
            }
            if (!notificationAlreadyExists) {
                db.collection("users")
                    .document(daterID)
                    .collection("matchNotifications")
                    .add(mapOf("suitorID" to userID))
                    .await()
            }

            db.collection("users").document(daterID).update("suitorID", userID).await()

            db.collection("users").document(daterID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Error fetching user snapshot: $error")
                        return@addSnapshotListener
                    }
                    val data = snapshot?.data ?: return@addSnapshotListener
                    val suitorID = data["suitorID"] as? String ?: return@addSnapshotListener
                    val passedID = data["fcmToken"] as? String ?: return@addSnapshotListener
                    val notificationPayload = mapOf(
                        "tapperID" to suitorID,
                        "tappedID" to passedID,
                        "tapperName" to candidate.name
                    )
                    FirebaseFunctions.getInstance().getHttpsCallable("notifyUser")
                        .call(notificationPayload)
                        .addOnCompleteListener { task: Task<HttpsCallableResult> ->
                            if (!task.isSuccessful) {
                                task.exception?.let { println("Error notifying new match: ${it.localizedMessage}") }
                            }
                        }
                }

            compatibleUsers.removeAt(index)
            selectedFriendIndex = null
            snackBarMessage.value = "Your request has been sent!"

        } catch (e: Exception) {
            errorAlertStateTitle.value = "Uh-oh"
            errorAlertState.value = "Unfortunately this user is no longer available."
            errorAlertStateAccept.value = "OK"
        }
    }

    private suspend fun canCalculate(): Boolean {
        val last = dataStoreManager.read(DataStoreKeys.lastCompatibleCalculation)
        if (last.isEmpty()) return true
        val lastDate = dateFormat.parse(last) ?: return true
        val elapsed = (Date().time - lastDate.time) / 1000
        return elapsed > (24 * 3600)
    }

    private suspend fun getLastKnownLocation(): Pair<Double, Double>? {
        if (ActivityCompat.checkSelfPermission(
                justFriends, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return null
        return try {
            val location = LocationServices.getFusedLocationProviderClient(justFriends)
                .lastLocation.await()
            location?.let { Pair(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}
