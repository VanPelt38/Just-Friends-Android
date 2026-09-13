package com.example.justfriends.Features.SettingsFeature

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class SettingsViewModel(
    justFriends: Application,
    private val dataStoreManager: DataStoreManager,
    private val currentView: MutableState<String>,
    private val navBarTitle: MutableState<String>,
    private val shouldShowFAB: MutableState<Boolean>
) : AndroidViewModel(justFriends) {

    private val db = FirebaseFirestore.getInstance()
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    // Separate flow observed by the root NavHost so logout/delete can reach the login screen
    private val _navigateToRoot = MutableStateFlow<String?>(null)
    val navigateToRoot: StateFlow<String?> = _navigateToRoot.asStateFlow()

    var isLoading = mutableStateOf(false)
    var distanceKm = mutableStateOf(10f)

    val alertState = mutableStateOf<String?>(null)
    val alertTitle = mutableStateOf<String?>(null)
    val alertAccept = mutableStateOf<String?>(null)
    val alertDecline = mutableStateOf<String?>(null)
    var pendingAction: (() -> Unit)? = null

    fun setScaffold() {
        currentView.value = ""
        navBarTitle.value = "Settings"
        shouldShowFAB.value = false
    }

    fun loadDistancePreference() {
        viewModelScope.launch {
            val stored = dataStoreManager.read(DataStoreKeys.distancePreference)
            distanceKm.value = if (stored.isNotEmpty()) (stored.toFloatOrNull() ?: 10000f) / 1000f else 10f
        }
    }

    fun saveDistancePreference(km: Float) {
        viewModelScope.launch {
            dataStoreManager.write(DataStoreKeys.distancePreference, (km * 1000).toInt().toString())
        }
    }

    fun confirmLogOut() {
        alertTitle.value = "Log Out"
        alertState.value = "Are you sure you want to log out?"
        alertAccept.value = "Log Out"
        alertDecline.value = "Cancel"
        pendingAction = { logOut() }
    }

    fun confirmDeleteAccount() {
        alertTitle.value = "Delete Account"
        alertState.value = "Are you sure? This will permanently delete your account and all your data."
        alertAccept.value = "Delete"
        alertDecline.value = "Cancel"
        pendingAction = { deleteAccount() }
    }

    fun logOut() {
        viewModelScope.launch {
            dataStoreManager.write(DataStoreKeys.loggedInHome, "")
            dataStoreManager.write(DataStoreKeys.onChatView, "")
            FirebaseAuth.getInstance().signOut()
            _navigateToRoot.value = View.login.name
        }
    }

    fun deleteAccount() {
        isLoading.value = true
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            val userID = auth.currentUser?.uid
            if (userID != null) {
                try {
                    deleteSubcollection("users", userID, "matchStatuses")
                    deleteSubcollection("users", userID, "blockedUsers")
                    deleteSubcollection("users", userID, "profile")
                    deleteSubcollection("users", userID, "registration")
                    deleteSubcollection("users", userID, "matchNotifications")
                    deleteSubcollection("users", userID, "expiringRequests")

                    val statuses = db.collection("statuses")
                        .whereEqualTo("userID", userID).get().await()
                    for (doc in statuses.documents) doc.reference.delete().await()

                    db.collection("users").document(userID).delete().await()
                    auth.currentUser?.delete()?.await()
                } catch (e: Exception) {
                    println("Error deleting account: ${e.message}")
                }
            }
            dataStoreManager.write(DataStoreKeys.loggedInHome, "")
            dataStoreManager.write(DataStoreKeys.loggedInProfile, "")
            dataStoreManager.write(DataStoreKeys.email, "")
            dataStoreManager.write(DataStoreKeys.password, "")
            isLoading.value = false
            _navigateToRoot.value = View.login.name
        }
    }

    private suspend fun deleteSubcollection(collection: String, documentID: String, subCollection: String) {
        val docs = db.collection(collection).document(documentID).collection(subCollection).get().await()
        for (doc in docs.documents) doc.reference.delete().await()
    }

    fun dismissAlert() {
        alertState.value = null
        alertTitle.value = null
        alertAccept.value = null
        alertDecline.value = null
        pendingAction = null
    }

    fun navigateToDistancePreference() {
        onNavigate(View.distancePreference.name)
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }

    fun onRootNavigationComplete() {
        _navigateToRoot.value = null
    }
}
