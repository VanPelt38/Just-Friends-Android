package com.example.justfriends.Features.DatePlannerFeature

import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import java.util.Date
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import android.app.Activity
import androidx.compose.runtime.Composable
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class DatePlannerViewModel(private val justFriends: Application,
                       private val dataStoreManager: DataStoreManager,
                       private val navBarTitle: MutableState<String>
): AndroidViewModel(justFriends) {

    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()
    var activity = mutableStateOf("")
    var selectedOption = mutableStateOf("select a time")
    private val _latitude = MutableStateFlow(0.0)
    val latitude = _latitude.asStateFlow()
    private val _longitude = MutableStateFlow(0.0)
    private lateinit var auth: FirebaseAuth
    val longitude = _longitude.asStateFlow()
    var isLoading = mutableStateOf(false)
    var locationPermission = mutableStateOf(false)
    private val _errorAlertState = mutableStateOf<String?>(null)
    private val db = FirebaseFirestore.getInstance()
    val errorAlertState = _errorAlertState
    private val _errorAlertStateTitle = mutableStateOf<String?>(null)
    val errorAlertStateTitle = _errorAlertStateTitle
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
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

    init {
        auth = FirebaseAuth.getInstance()
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun setNavTitle() {
        navBarTitle.value = "Planner"
    }

    fun requestLocationPermission(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(justFriends, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    fun seeWhosAvailablePressed() {

        isLoading.value = true
        if (selectedOption.value != "select a time" && activity.value != "") {
            if (ActivityCompat.checkSelfPermission(justFriends, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                viewModelScope.launch {
                    addStatusToFireStore()
                    isLoading.value = false
                    onNavigate(View.availablePeople.name)
                }

            } else {
                isLoading.value = false
                _errorAlertStateTitle.value = "Uh-oh"
                _errorAlertState.value = "It seems you haven't given Just Friends permission to access your location. Please go to 'Settings', 'Just Friends', 'Permissions', and then 'Location', in order to do so."
            }
        } else {
            isLoading.value = false
            _errorAlertStateTitle.value = "Uh-oh"
            _errorAlertState.value = "Please select a time and activity."
        }
    }

    suspend fun addStatusToFireStore() {

        val userID = auth.currentUser?.uid
        val token = FirebaseMessaging.getInstance().token.await()
        val newStatusData = mapOf(
            "activity" to activity.value,
            "time" to selectedOption.value,
            "userID" to userID,
            "fcmToken" to token,
            "latitude" to latitude.value,
            "longitude" to longitude.value,
            "timeStamp" to Date()
        )

        try {
            val existingStatus = db.collection("statuses")
                .whereEqualTo("userID", userID)
                .limit(1)
                .get()
                .await()

            if (existingStatus.documents.isNotEmpty()) {
                val status = existingStatus.documents.first()
                status.reference.set(newStatusData).await()
            } else {
                val e = db.collection("statuses")
                    .add(newStatusData)
                    .await()
            }
        } catch(e: Exception) {
            println("error saving a new status")
        }
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}