package com.example.justfriends.Features.UserProfileFeature

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class EditProfileViewModel(
    justFriends: Application,
    private val dataStoreManager: DataStoreManager,
    private val currentView: MutableState<String>,
    private val navBarTitle: MutableState<String>,
    private val shouldShowFAB: MutableState<Boolean>
) : AndroidViewModel(justFriends) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var isLoading = mutableStateOf(false)
    var town = mutableStateOf("")
    var occupation = mutableStateOf("")
    var summary = mutableStateOf("")
    var selectedInterests = mutableStateOf<Set<String>>(emptySet())
    val errorAlertState = mutableStateOf<String?>(null)
    var savedSuccessfully = mutableStateOf(false)

    val allInterests = listOf(
        "Sports", "Exercise", "Reading", "Foodie", "Nightlife", "Gardening",
        "Socialising", "Music", "Gaming", "Cinema", "Cooking", "Travelling",
        "Art", "Tech", "Photography", "Writing", "Walking", "Pubs"
    )

    fun setScaffold() {
        currentView.value = ""
        navBarTitle.value = "Edit Profile"
        shouldShowFAB.value = true
    }

    fun loadProfile() {
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
                town.value = data["town"] as? String ?: ""
                occupation.value = data["occupation"] as? String ?: ""
                summary.value = data["summary"] as? String ?: ""
                val loaded = (data["interests"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                selectedInterests.value = loaded.toSet()
            }
        }
    }

    fun toggleInterest(interest: String) {
        val current = selectedInterests.value.toMutableSet()
        if (current.contains(interest)) {
            current.remove(interest)
        } else {
            if (current.size >= 5) {
                errorAlertState.value = "You can select a maximum of 5 interests."
                return
            }
            current.add(interest)
        }
        selectedInterests.value = current
    }

    fun saveProfile() {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val userID = auth.currentUser?.uid ?: return@launch
                val data = mapOf(
                    "town" to town.value,
                    "occupation" to occupation.value,
                    "summary" to summary.value,
                    "interests" to selectedInterests.value.toList()
                )
                db.collection("users")
                    .document(userID)
                    .collection("profile")
                    .document("profile")
                    .set(data, SetOptions.merge())
                    .await()
                savedSuccessfully.value = true
            } catch (e: Exception) {
                errorAlertState.value = "Failed to save profile. Please try again."
            }
            isLoading.value = false
        }
    }
}
