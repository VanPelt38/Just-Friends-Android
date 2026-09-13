package com.example.justfriends.Features.UserProfileFeature

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class UserProfileViewModel(
    justFriends: Application,
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
    var name = mutableStateOf("")
    var age = mutableStateOf("")
    var gender = mutableStateOf("")
    var town = mutableStateOf<String?>(null)
    var occupation = mutableStateOf<String?>(null)
    var summary = mutableStateOf<String?>(null)
    var interests = mutableStateOf<List<String>>(emptyList())
    var profilePicUrl = mutableStateOf<String?>(null)
    var uri = mutableStateOf<Uri?>(null)
    var imageExt: String? = null

    val errorAlertState = mutableStateOf<String?>(null)
    val errorAlertStateTitle = mutableStateOf<String?>(null)

    fun setScaffold() {
        currentView.value = ""
        navBarTitle.value = "My Profile"
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
                name.value = data["name"] as? String ?: ""
                age.value = (data["age"] as? Number ?: 0).toString()
                gender.value = data["gender"] as? String ?: ""
                town.value = data["town"] as? String
                occupation.value = data["occupation"] as? String
                summary.value = data["summary"] as? String
                interests.value = (data["interests"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                profilePicUrl.value = data["picture"] as? String
            }
        }
    }

    fun onPhotoSelected(newUri: Uri?, extension: String?) {
        uri.value = newUri
        imageExt = extension
        if (newUri == null) return
        val valid = listOf("jpg", "png", "jpeg", "heic")
        if (!valid.contains(extension?.lowercase())) {
            errorAlertStateTitle.value = "Unsupported Format"
            errorAlertState.value = "Please use an image with extension: heic, jpeg, jpg, or png."
            return
        }
        uploadNewPhoto(newUri)
    }

    private fun uploadNewPhoto(photoUri: Uri) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val userID = auth.currentUser?.uid ?: return@launch
                val storageRef = FirebaseStorage.getInstance().reference
                val fileName = "${UUID.randomUUID()}.jpg"
                val imageRef = storageRef.child("images/$fileName")
                imageRef.putFile(photoUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                db.collection("users")
                    .document(userID)
                    .collection("profile")
                    .document("profile")
                    .update("picture", downloadUrl, "profilePicRef", fileName)
                    .await()

                profilePicUrl.value = downloadUrl
            } catch (e: Exception) {
                errorAlertStateTitle.value = "Upload Failed"
                errorAlertState.value = "Could not upload photo. Please try again."
            }
            isLoading.value = false
        }
    }

    fun navigateToEditProfile() {
        _navigateTo.value = View.editProfile.name
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}
