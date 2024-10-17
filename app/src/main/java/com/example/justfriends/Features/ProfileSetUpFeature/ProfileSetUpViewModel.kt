package com.example.justfriends.Features.ProfileSetUpFeature

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.database.Cursor
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.example.justfriends.Utils.NetworkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import java.util.Date

class ProfileSetUpViewModel(justFriends: Application, private val dataStoreManager: DataStoreManager): AndroidViewModel(justFriends) {

    private val connectivityObserver = NetworkManager(justFriends)
    private var networkIsConnected = true
    val genderOptions = listOf("M", "F")
    var uri by mutableStateOf<Uri?>( null)
    val context = getApplication<Application>().applicationContext
    var imageExt: String? = null
    var uploadedImageURL: String? = null
    var selectedGender by mutableStateOf(genderOptions[0])
    var name = mutableStateOf("")
    var dobString = mutableStateOf<String?>(null)
    var dobDate: Date? = null
    var isLoading = mutableStateOf(false)
    private lateinit var auth: FirebaseAuth
    var profilePicRef: String = ""
    private val db = FirebaseFirestore.getInstance()
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()
    private val _errorAlertState = mutableStateOf<String?>(null)
    val errorAlertState = _errorAlertState
    private val _errorAlertStateTitle = mutableStateOf<String?>(null)
    val errorAlertStateTitle = _errorAlertStateTitle

    init {
        auth = FirebaseAuth.getInstance()
        viewModelScope.launch {
            connectivityObserver.observeNetworkStatus().collect { isConnected ->
                networkIsConnected = isConnected
            }
        }
    }

    fun selectOption(option: String) {
        if (option in genderOptions) {
            selectedGender = option
        }
    }

    fun calculateAge(): Int {
        var age: Int = 0
        if (dobDate != null) {
        val birthCalendar = Calendar.getInstance()
            val todayCalendar = Calendar.getInstance()

            birthCalendar.time = dobDate

            val birthYear = birthCalendar.get(Calendar.YEAR)
            val birthMonth = birthCalendar.get(Calendar.MONTH)
            val birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH)
            val currentYear = todayCalendar.get(Calendar.YEAR)
            val currentMonth = todayCalendar.get(Calendar.MONTH)
            val currentDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

            age = currentYear - birthYear

            if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                age--
            }
        }
        return age
    }

    fun getImageExtension() {
        val cursor: Cursor? = context.contentResolver.query(uri!!, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) {
                if (it.moveToFirst()) {
                    val displayName = it.getString(index)
                    imageExt = displayName.substringAfterLast('.', "")
                    Log.d("", "$imageExt")
                }
            }
        }
    }

    fun isValidExtension(): Boolean {
        val validExts = listOf("jpg", "png", "jpeg", "heic")
        return validExts.contains(imageExt!!.lowercase())
    }

    suspend fun uploadImageToFirebase() {
        try {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            val imageFileName: String = "${UUID.randomUUID()}.jpg"
            profilePicRef = imageFileName
            val imageRef: StorageReference = storageRef.child("images/$imageFileName")
            imageRef.putFile(uri!!).await()
            val downloadUrl = imageRef.downloadUrl.await()
            uploadedImageURL = downloadUrl.toString()
        } catch(e: Exception) {
            println("upload image failed: ${e.message}")
        }
    }

    suspend fun saveProfileToFirestore(age: Int) {

        val userID = auth.currentUser?.uid
        val batch: WriteBatch = db.batch()
        val collection1 = db.collection("users")
            .document(userID ?: "")
            .collection("profile")
            .document("profile")
        val collection2 = db.collection("users").document(userID ?: "")
        val profileData = mapOf(
            "age" to age,
            "gender" to selectedGender,
            "name" to name.value,
            "picture" to uploadedImageURL,
            "userID" to userID,
            "profilePicRef" to profilePicRef
        )
        batch.set(collection1, profileData)

        val existenceData = mapOf(
            "existence" to "yes"
        )
        batch.set(collection2, existenceData, SetOptions.merge())

        try {
            batch.commit().await()
        } catch (e: Exception) {
            println("error with batch write create proifle: ${e.message}")
        }
    }

    suspend fun flagProfileSetUp() {

        val userID = auth.currentUser?.uid
        val profileSetUpData = mapOf(
            "profileSetUp" to true
        )
        try {
            val userCollection = db.collection("users")
                .document(userID ?: "")
                .collection("registration")
                .document(userID ?: "")
                .set(profileSetUpData)
                .await()
        } catch(e: Exception) {
            println("error flagging profile set up")
        }

    }

    fun profileCompletePressed() {

        isLoading.value = true
        if (networkIsConnected) {
            if (uri != null && name.value != "" && dobString.value != null) {
                if (isValidExtension()) {

                    viewModelScope.launch {
                        uploadImageToFirebase()
                        val age = calculateAge()
                        saveProfileToFirestore(age = age)
                        flagProfileSetUp()
                        dataStoreManager.write(DataStoreKeys.loggedInHome, "true")
                        isLoading.value = false
                        onNavigate(View.main.name)
                    }

                } else {
                    isLoading.value = false
                    _errorAlertStateTitle.value = "Uh-oh"
                    _errorAlertState.value =
                        "We're sorry but the image file you've chosen is unsupported - please use images with the following extensions: heic, jpeg, jpg, png."
                }
            } else {
                isLoading.value = false
                _errorAlertStateTitle.value = "Profile Incomplete"
                _errorAlertState.value = "Please enter all your details before proceeding."
            }
        } else {
            isLoading.value = false
            _errorAlertStateTitle.value = "No Internet"
            _errorAlertState.value = "Please check your network connection and try again."
        }
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}