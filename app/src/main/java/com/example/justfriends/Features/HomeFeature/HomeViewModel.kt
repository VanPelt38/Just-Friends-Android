package com.example.justfriends.Features.HomeFeature

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class HomeViewModel(justFriends: Application,
                    private val dataStoreManager: DataStoreManager,
    private val navBarTitle: MutableState<String>
): AndroidViewModel(justFriends) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    var userName = mutableStateOf("")
    var uri by mutableStateOf<Uri?>( null)
    private set
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    init {
        auth = FirebaseAuth.getInstance()
        setFalseForOnChatView()
        loadUserData()
        setDistancePreference()
    }

    fun setFalseForOnChatView() {
        viewModelScope.launch {
            dataStoreManager.write(DataStoreKeys.onChatView, "false")
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            loadProfile()
            setFcmTokenAndExistence()
        }
    }

    suspend fun setFcmTokenAndExistence() {

        val userID = auth.currentUser?.uid
        val token = FirebaseMessaging.getInstance().token.await()
        val newData = mapOf(
            "fcmToken" to token,
            "existence" to "yes"
        )
        try {
            val set = db.collection("users")
                .document(userID ?: "")
                .update(newData)
                .await()
        } catch (e: Exception) {
            println("error flagging fcm token and existence")
        }
    }

    suspend fun loadProfile() {

        val userID = auth.currentUser?.uid

        viewModelScope.launch {
            val userProfileQuery = db.collection("users")
                .document(userID ?: "")
                .collection("profile")
                .whereEqualTo("userID", userID)
                .limit(1)

            val userProfile = userProfileQuery.get().await()

            if (!userProfile.isEmpty()) {
                for (doc in userProfile.documents) {
                    val data = doc.data
                    if (data != null) {
                        navBarTitle.value = "Hi ${data["name"] as? String ?: ""}"
                        val imageURL = data["picture"] as? String ?: ""
                        uri = imageURL.let { Uri.parse(it) }
                    }
                }
            }
        }
    }

    fun setDistancePreference() {
        viewModelScope.launch {
            if (dataStoreManager.read(DataStoreKeys.distancePreference) == "") {
                    dataStoreManager.write(DataStoreKeys.distancePreference, "10000")
            }
        }
    }

   fun navigateToProfile() {
       onNavigate(View.userProfile.name)
   }

    fun navigateToMostCompatible() {
        onNavigate(View.mostCompatible.name)
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}