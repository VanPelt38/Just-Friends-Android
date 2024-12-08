package com.example.justfriends.Features.AvailablePeopleFeature

import android.app.Application
import android.content.pm.PackageManager
import java.util.Date
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.app.ActivityCompat
import android.Manifest
import android.location.Location
import androidx.compose.runtime.mutableStateListOf
import com.example.justfriends.DataModels.ExpiringMatch
import com.example.justfriends.DataModels.MatchModel
import com.example.justfriends.DataModels.Status
import com.example.justfriends.DataModels.User
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat


class AvailablePeopleViewModel(private val justFriends: Application,
                           private val dataStoreManager: DataStoreManager,
                           private val navBarTitle: MutableState<String>,
    private val navBarAction: MutableState<() -> Unit>,
                               private val notificationCount: MutableState<Int>
): AndroidViewModel(justFriends) {

    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()
    var isLoading = mutableStateOf(false)
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    var userProfile: User? = null
    var activityPlan = mutableStateOf<String?>("hang out")
    var activityTime = mutableStateOf<String?>("today")
    var people = mutableStateListOf<Status>()
    var expiringMatchesArray = arrayListOf<ExpiringMatch>()
    var matchStatusesArray = arrayListOf<MatchModel>()
    var profiles = mutableStateListOf<User>()
    private val _latitude = MutableStateFlow(0.0)
    val lati = _latitude.asStateFlow()
    private val _longitude = MutableStateFlow(0.0)
    val longi = _longitude.asStateFlow()
    private val _errorAlertState = mutableStateOf<String?>(null)
    private val _errorAlertStateTitle = mutableStateOf<String?>(null)
    private val _errorAlertStateAccept = mutableStateOf<String?>(null)
    private val _errorAlertStateDecline = mutableStateOf<String?>(null)
    val errorAlertState = _errorAlertState
    val errorAlertStateTitle = _errorAlertStateTitle
    val errorAlertStateAccept = _errorAlertStateAccept
    val errorAlertStateDecline = _errorAlertStateDecline
    private val _snackBarMessage = mutableStateOf<String?>(null)
    val snackBarMessage = _snackBarMessage
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(justFriends)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            p0.locations.forEach { location ->
                _latitude.value = location.latitude
                _longitude.value = location.longitude
            }
        }
    }
    var distancePreference: String = ""
    var lastShareDate: String = ""
    var fcmToken: String? = null
    var selectedFriendIndex: Int? = null

    init {
        auth = FirebaseAuth.getInstance()
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun loadAll() {

        viewModelScope.launch {
            listenForNewNotifications()
            requestLocationUpdates()
            getDistancePreferenceAndLastShareDate()
            getFCMToken()
            getNotifications()
            val loadUserStatusJob = async { loadUserStatus() }
            val loadUserProfileJob = async { loadUserProfile() }
            val loadExpiringMatchesJob = async { loadExpiringMatches() }
            val filterExpiringMatchesJob = async { filterExpiringMatches() }
            loadUserStatusJob.await()
            loadUserProfileJob.await()
            loadExpiringMatchesJob.await()
            filterExpiringMatchesJob.await()
            val loadStatusesJob = async { loadStatuses() }
            loadStatusesJob.await()
            removeFarLocations()
            removeExpiredStatuses()
            removeExpiredMatches()
            val removeBlockedUsersJob = async { removeBlockedUsers() }
            removeBlockedUsersJob.await()
            loadProfiles()
            loadExistingMatches()
        }
    }

    suspend fun getFCMToken() {
        fcmToken = FirebaseMessaging.getInstance().token.await()
    }

    suspend fun loadExistingMatches() {
        val userID = auth.currentUser?.uid

        val matches = db.collection("users")
            .document(userID ?: "")
            .collection("matchStatuses")
            .get()
            .await()

        if (!matches.isEmpty) {
            for (doc in matches.documents) {
                val data = doc.data
                if (data != null) {
                    val match = MatchModel(
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
                        picture = data["picture"] as? String ?: "error",
                        ID = data["userID"] as? String ?: "error"
                    )
                    matchStatusesArray.add(match)
                }
            }
        }
    }

    fun startMatching() {
        if (alreadyMatched()) {
            _errorAlertStateTitle.value = "Uh-oh"
            _errorAlertState.value = "Looks like the two of you are already connected. Try sending them a message instead."
            _errorAlertStateAccept.value = "Okay"
        } else {
            viewModelScope.launch {
                matchWithUser()
            }
        }
    }

    suspend fun matchWithUser() {
        selectedFriendIndex?.let { index ->
            userProfile?.let { myProfile ->

            val userID = auth.currentUser?.uid
            val dateFirebaseDocID = people[index].firebaseDocID
            val dateID = people[index].daterID
                val dateName = profiles[index].name
            val friendStatus = db.collection("statuses")
                .document(dateFirebaseDocID)
                .get()
                .await()
            if (friendStatus.exists()) {
                val expiringRequestData = mapOf(
                    "timeStamp" to Date(),
                    "userID" to dateID,
                    "ownUserID" to userID,
                )
                db.collection("users")
                    .document(userID ?: "")
                    .collection("expiringRequests")
                    .document(dateID)
                    .set(expiringRequestData)
                    .await()

                val matchStatusData = mapOf(
                    "name" to myProfile.name,
                    "imageURL" to myProfile.picture,
                    "activity" to activityPlan.value,
                    "time" to activityTime.value,
                    "ID" to userID,
                    "age" to myProfile.age.toIntOrNull(),
                    "gender" to myProfile.gender,
                    "accepted" to false,
                    "fcmToken" to fcmToken,
                    "realmID" to "androidUser",
                    "ownUserID" to dateID,
                    "chatID" to "none",
                    "distanceAway" to people[index].distanceAway
                )

                db.collection("users")
                    .document(dateID)
                    .collection("matchStatuses")
                    .document(userID ?: "")
                    .set(matchStatusData)
                    .await()

                var notificationAlreadyExists = false

               val existingMatchNotifications =  db.collection("users")
                   .document(dateID)
                   .collection("matchNotifications")
                   .get()
                   .await()

                if (!existingMatchNotifications.isEmpty) {
                    for (doc in existingMatchNotifications.documents) {
                        val data = doc.data
                        if (data != null) {
                            val suitorID = data["suitorID"] as? String ?: "error"
                            if (suitorID == userID) {
                                notificationAlreadyExists = true
                            }
                        }
                    }
                }
                if (!notificationAlreadyExists) {

                    val matchNotificationData = mapOf(
                        "suitorID" to userID
                    )
                    db.collection("users")
                        .document(dateID)
                        .collection("matchNotifications")
                        .add(matchNotificationData)
                        .await()
                }
                db.collection("statuses")
                    .document(dateFirebaseDocID)
                    .update("suitorID", userID ?: "")
                    .await()

                db.collection("statuses")
                    .document(dateFirebaseDocID)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            println("Error fetching statuses snapshot: $error")
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                           val data = snapshot.data
                            if (data != null) {
                                val suitorID = data["suitorID"] as? String ?: "error"
                                val passedID = data["fcmToken"] as? String ?: "error"
                                val myFunctions = FirebaseFunctions.getInstance()

                                val notificationPayload = mapOf(
                                    "tapperID" to suitorID,
                                    "tappedID" to passedID,
                                    "tapperName" to dateName
                                )
                                myFunctions.getHttpsCallable("notifyUser")
                                    .call(notificationPayload)
                                    .addOnCompleteListener { task: Task<HttpsCallableResult> ->
                                        if (!task.isSuccessful) {
                                            task.exception?.let {  exception ->
                                                println("Error notifying new match: ${exception.localizedMessage}")
                                            }
                                        } else {
                                            task.result?.data?.let { result ->
                                                 println("Success notifying new match: ${result}")
                                            }
                                        }
                                    }
                            }
                        }
                    }
                people.removeAt(index)
                profiles.removeAt(index)
                snackBarMessage.value = "Your request has been sent!"
            } else {
                _errorAlertStateTitle.value = "Uh-oh"
                _errorAlertState.value = "Unfortunately this user is no longer available."
                _errorAlertStateAccept.value = "Okay"
            }
        }
        }
    }

    fun alreadyMatched(): Boolean {
        selectedFriendIndex?.let { index ->
            return matchStatusesArray.any { it.ID == people[index].daterID }
        }
        return false
    }

    fun seeFriendProfile() {
        onNavigate(View.friendProfile.name)
    }

    suspend fun listenForNewNotifications() {

        val userID = auth.currentUser?.uid
        db.collection("users")
            .document(userID ?: "")
            .collection("matchNotifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error fetching notifications snapshot: $error")
                    return@addSnapshotListener
                }
                snapshot?.let {
                   for (change in it.documentChanges)  {
                       if (change.type == DocumentChange.Type.ADDED) {
                           viewModelScope.launch {
                               getNotifications()
                           }
                       }
                   }
            }
            }
    }

    suspend fun getDistancePreferenceAndLastShareDate() {
        distancePreference = dataStoreManager.read(DataStoreKeys.distancePreference)
        lastShareDate = dataStoreManager.read(DataStoreKeys.lastShareDate)
    }

    fun requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                justFriends,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    suspend fun loadUserProfile() {

        val userID = auth.currentUser?.uid
        val profile = db.collection("users")
            .document(userID ?: "")
            .collection("profile")
            .whereEqualTo("userID", userID)
            .limit(1)
            .get()
            .await()

        if (!profile.isEmpty) {
            for (doc in profile.documents) {
                val data = doc.data
                if (data != null) {
                    val user = User(
                        age = (data["age"] as? Number ?: 0).toString(),
                        gender = data["gender"] as? String ?: "error",
                        name = data["name"] as? String ?: "error",
                        picture = data["picture"] as? String ?: "error",
                        userID = data["userID"] as? String ?: "error"
                    )
                    userProfile = user
                }
            }
        }
    }

    suspend fun loadProfiles() {

            for (person in people) {
                val profile = db.collection("users")
                    .document(person.daterID)
                    .collection("profile")
                    .whereEqualTo("userID", person.daterID)
                    .limit(1)
                    .get()
                    .await()

                if (!profile.isEmpty) {
                    for (doc in profile.documents) {
                        val data = doc.data
                        if (data != null) {

                            val user = User(
                                age = (data["age"] as? Number ?: 0).toString(),
                                gender = data["gender"] as? String ?: "error",
                                name = data["name"] as? String ?: "error",
                                picture = data["picture"] as? String ?: "error",
                                userID = data["userID"] as? String ?: "error"
                            )
                            profiles.add(user)
                        }
                    }
                }
            }
        isLoading.value = false
        showShareAlert()
    }

    fun showShareAlert() {
        if (timeHasElapsed()) {
            _errorAlertStateTitle.value = "Share with friends!"
            _errorAlertState.value = "Spread the word about our app and start building a community."
            _errorAlertStateAccept.value = "Share"
            _errorAlertStateDecline.value = "Not right now"
        }
    }

    fun timeHasElapsed(): Boolean {

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (lastShareDate != "") {
            val shareDate: Date = format.parse(lastShareDate)
            val timeInterval = (Date().time - shareDate.time) / 1000
            if (timeInterval > (48 * 3600)) {
                viewModelScope.launch {
                    dataStoreManager.write(DataStoreKeys.lastShareDate, format.format(Date()))
                }
                return true
            } else {
                return false
            }
        } else {
            viewModelScope.launch {
                dataStoreManager.write(DataStoreKeys.lastShareDate, format.format(Date()))
            }
            return true
        }
    }

    suspend fun removeBlockedUsers() {
        val userID = auth.currentUser?.uid
        var blockIDs = arrayListOf<String>()
        var returnArray = people

            val blockedUsers = db.collection("users")
                .document(userID ?: "")
                .collection("blockedUsers")
                .get()
                .await()
            if (!blockedUsers.isEmpty) {
                for (doc in blockedUsers.documents) {
                    val data = doc.data
                    if (data != null) {
                        blockIDs.add(data["blockedUserID"] as? String ?:"")
                    }
                }
                people.forEachIndexed { index, person ->
                    if (blockIDs.contains(person.daterID) ) {
                        returnArray.removeAt(index)
                    }
                }
                people = returnArray
            }

}
    fun removeExpiredMatches() {
        var returnArray = people
        var expiredIDs = arrayListOf<String>()
        for (match in expiringMatchesArray) {
            expiredIDs.add(match.userID)
        }
         people.forEachIndexed { index, person ->
             if (expiredIDs.contains(person.daterID) ) {
                 returnArray.removeAt(index)
             }
         }
        people = returnArray
    }

    fun removeExpiredStatuses() {

        val filteredStatuses = mutableStateListOf<Status>()
        val currentTime = Date()
        for (person in people)  {
            val expiryTime = person.timeStamp.let {
                Date(it.time + 12 * 60 * 60 * 1000)
            }
            if (currentTime <= expiryTime) {
               filteredStatuses.add(person)
            }
        }
       people = filteredStatuses
    }

    fun removeFarLocations() {

        val filteredStatuses = mutableStateListOf<Status>()
        val userLocation = Location("").apply {
            latitude = lati.value
            longitude = longi.value
        }
        for (person in people) {
            val personsLocation = Location("").apply {
                latitude = person.latitude
                longitude = person.longitude
            }
            if (personsLocation.distanceTo(userLocation) <= (distancePreference as? Int ?: 10000)) {
                val distance = (personsLocation.distanceTo(userLocation) as? Int ?: 10000) / 1000
                person.distanceAway = if (distance >= 1) distance else 1
                  filteredStatuses.add(person)
            }
        }
        people = filteredStatuses
    }

    suspend fun loadStatuses() {
        val userID = auth.currentUser?.uid

            val statuses = db.collection("statuses")
                .whereNotEqualTo("userID", userID)
                .get()
                .await()
            if (!statuses.isEmpty) {
                for (doc in statuses.documents) {
                    val data = doc.data
                    if (data != null) {
                        val timeStamp = data["timeStamp"] as? Timestamp
                        val timeDate = timeStamp?.toDate()

                       val status = Status(
                           activity = data["activity"] as? String ?: "error",
                           fcmToken = data["fcmToken"] as? String ?: "error",
                           longitude = data["longitude"] as? Double ?: 0.0,
                           latitude = data["latitude"] as? Double ?: 0.0,
                           time = data["time"] as? String ?: "error",
                           timeStamp = timeDate ?: Date(),
                           daterID = data["userID"] as? String ?: "error",
                           firebaseDocID = doc.id as? String ?: "error"
                       )
                        people.add(status)
                    }
                }
            }
    }

    suspend fun filterExpiringMatches() {
        val userID = auth.currentUser?.uid
        val filteredMatches = arrayListOf<ExpiringMatch>()

        for (match in expiringMatchesArray) {
            val currentTime = Date()
            val matchTimeStamp = match.timeStamp.toInstant().plusSeconds(10800)
            val matchDate = Date.from(matchTimeStamp)

            if (matchDate > currentTime) {
                filteredMatches.add(match)
            } else {
                db.collection("users")
                    .document(userID ?: "")
                    .collection("expiringRequests")
                    .document(match.userID)
                    .delete()
                    .await()
            }
        }
        expiringMatchesArray = filteredMatches
    }

    suspend fun loadExpiringMatches() {
        val userID = auth.currentUser?.uid

            val expiringMatches = db.collection("users")
                .document(userID ?: "")
                .collection("expiringRequests")
                .get()
                .await()

            if (!expiringMatches.isEmpty) {
                for (doc in expiringMatches.documents) {
                    val data = doc.data
                    if (data != null) {

                        val time = data["timeStamp"] as? Timestamp
                        val timeStamp = time?.toDate()

                       val expiringMatch = ExpiringMatch(
                           ownUserID = data["ownUserID"] as? String ?: "error",
                           timeStamp = timeStamp ?: Date(),
                           userID = data["userID"] as? String ?: "error"
                       )
                        expiringMatchesArray.add(expiringMatch)
                    }
                }
            }

    }

    suspend fun loadUserStatus() {
        val userID = auth.currentUser?.uid

            val userStatus = db.collection("statuses")
                .whereEqualTo("userID", userID)
                .limit(1)
                .get()
                .await()

            if (!userStatus.isEmpty) {
                for (doc in userStatus.documents) {
                    val data = doc.data
                    if (data != null) {
                        activityPlan.value = data["activity"] as? String
                        activityTime.value = data["time"] as? String
                    }
                }
            }
    }

    suspend fun getNotifications() {
        val userID = auth.currentUser?.uid

            val notifications = db.collection("users")
                .document(userID ?: "")
                .collection("matchNotifications")
                .get()
                .await()
            if (!notifications.isEmpty()) {
                var totalNotifications: Int = 0
                for (notification in notifications.documents) {
                    totalNotifications += 1
                }
                notificationCount.value = totalNotifications
            }
    }

    fun setNavTitleAndAction() {
        navBarTitle.value = "Available"
        navBarAction.value = { navigateToFriends() }
    }

    fun navigateToFriends() {
        onNavigate(View.friends.name)
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}