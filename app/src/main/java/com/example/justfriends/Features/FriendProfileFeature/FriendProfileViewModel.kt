package com.example.justfriends.Features.FriendProfileFeature

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class FriendProfileViewModel(
    justFriends: Application,
    private val dataStoreManager: DataStoreManager,
    private val currentView: MutableState<String>,
    private val navBarTitle: MutableState<String>,
    private val shouldShowFAB: MutableState<Boolean>
) : AndroidViewModel(justFriends) {

    private val db = FirebaseFirestore.getInstance()

    var isLoading = mutableStateOf(false)
    var name = mutableStateOf("")
    var age = mutableStateOf("")
    var gender = mutableStateOf("")
    var town = mutableStateOf<String?>(null)
    var occupation = mutableStateOf<String?>(null)
    var summary = mutableStateOf<String?>(null)
    var interests = mutableStateOf<List<String>>(emptyList())
    var profilePicUrl = mutableStateOf<String?>(null)

    fun setScaffold() {
        currentView.value = ""
        navBarTitle.value = "Profile"
        shouldShowFAB.value = true
    }

    fun loadProfile() {
        isLoading.value = true
        viewModelScope.launch {
            val friendID = dataStoreManager.read(DataStoreKeys.friendIDForProfile)
            if (friendID.isEmpty()) {
                isLoading.value = false
                return@launch
            }

            val docs = db.collection("users")
                .document(friendID)
                .collection("profile")
                .whereEqualTo("userID", friendID)
                .limit(1)
                .get()
                .await()

            if (!docs.isEmpty) {
                val data = docs.documents[0].data ?: run { isLoading.value = false; return@launch }
                name.value = data["name"] as? String ?: ""
                age.value = (data["age"] as? Number ?: 0).toString()
                gender.value = data["gender"] as? String ?: ""
                town.value = data["town"] as? String
                occupation.value = data["occupation"] as? String
                summary.value = data["summary"] as? String
                interests.value = (data["interests"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                profilePicUrl.value = data["picture"] as? String
            }
            isLoading.value = false
        }
    }
}
