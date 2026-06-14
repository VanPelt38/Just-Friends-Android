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
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

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

            val userID = auth.currentUser?.uid ?: run { isLoading.value = false; return@launch }
            val payload = mapOf(
                "userID" to userID,
                "latitude" to location.first,
                "longitude" to location.second
            )

            FirebaseFunctions.getInstance()
                .getHttpsCallable("calculateCompatibility")
                .call(payload)
                .addOnCompleteListener { task: Task<HttpsCallableResult> ->
                    if (task.isSuccessful) {
                        val resultData = task.result?.data
                        compatibleUsers.clear()
                        (resultData as? List<*>)?.forEach { item ->
                            val map = item as? Map<*, *> ?: return@forEach
                            compatibleUsers.add(
                                Compatible(
                                    name = map["name"] as? String ?: "",
                                    age = (map["age"] as? Number ?: 0).toString(),
                                    gender = map["gender"] as? String ?: "",
                                    picture = map["picture"] as? String,
                                    userID = map["userID"] as? String ?: "",
                                    town = map["town"] as? String,
                                    occupation = map["occupation"] as? String,
                                    summary = map["summary"] as? String,
                                    interests = (map["interests"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                                    distanceAway = (map["distanceAway"] as? Number ?: 0).toInt()
                                )
                            )
                        }
                        viewModelScope.launch {
                            dataStoreManager.write(DataStoreKeys.lastCompatibleCalculation, dateFormat.format(Date()))
                        }
                    } else {
                        errorAlertStateTitle.value = "Uh-oh"
                        errorAlertState.value = "Could not calculate compatibility. Please try again."
                        errorAlertStateAccept.value = "OK"
                    }
                    isLoading.value = false
                }
        }
    }

    fun seeFriendProfileForIndex(index: Int) {
        viewModelScope.launch {
            dataStoreManager.write(DataStoreKeys.friendIDForProfile, compatibleUsers[index].userID)
            _navigateTo.value = View.friendProfile.name
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

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}
