package com.example.justfriends.Features.LoginFeature

import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginViewModel(justFriends: Application, private val dataStoreManager: DataStoreManager): AndroidViewModel(justFriends) {

    var userEmail = mutableStateOf("")
    var userPassword = mutableStateOf("")
    private val _errorAlertState = mutableStateOf<String?>(null)
    val errorAlertState = _errorAlertState
    private val _errorAlertStateTitle = mutableStateOf<String?>(null)
    val errorAlertStateTitle = _errorAlertStateTitle
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    init {
        viewModelScope.launch {
            userEmail.value = dataStoreManager.read(DataStoreKeys.email)
            userPassword.value = dataStoreManager.read(DataStoreKeys.password)
        }
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination

    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }

    fun signInPressed() {
        auth = Firebase.auth

        if (userEmail.value != "" && userPassword.value != "") {
            viewModelScope.launch {
                login(email = userEmail.value, password = userPassword.value)
            }
        } else {
            _errorAlertState.value = "Please enter a valid email and password."
        }
    }

    fun signUpPressed() {
        auth = Firebase.auth

        if (userEmail.value != "" && userPassword.value != "") {

            viewModelScope.launch {
                dataStoreManager.write(DataStoreKeys.email, userEmail.value)
                dataStoreManager.write(DataStoreKeys.password, userPassword.value)
            }
            auth.createUserWithEmailAndPassword(userEmail.value, userPassword.value)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            login(email = userEmail.value, password = userPassword.value)
                        }
                    } else {
                        _errorAlertStateTitle.value = "Uh-oh"
                        _errorAlertState.value =
                            "There was an error registering: ${task.exception?.localizedMessage}"
                    }
                }
        } else {
            _errorAlertState.value = "Please enter a valid email and password."
        }
    }

    suspend fun login(email: String, password: String) {
        auth = Firebase.auth

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
            if (task.isSuccessful) {

                val userID = auth.currentUser?.uid

                if (userID != null) {

                    viewModelScope.launch {
                        dataStoreManager.write(DataStoreKeys.email, userEmail.value)
                        dataStoreManager.write(DataStoreKeys.password, userPassword.value)


                            val userCollection = db.collection("users")
                                .document(userID)
                                .collection("registration")
                                .document(userID)
                                .get()
                                .await()

                            if (userCollection.exists()) {

                                val data = userCollection.data
                                if (data != null) {
                                    if (data["profileSetUp"] as? Boolean ?: false) {

                                            dataStoreManager.write(
                                                DataStoreKeys.loggedInHome,
                                                "true"
                                            )

                                        onNavigate(View.main.name)
                                    } else {
                                            dataStoreManager.write(
                                                DataStoreKeys.loggedInProfile,
                                                "true"
                                            )

                                        onNavigate(View.profileSetUp.name)
                                    }
                                }
                            } else {
                                val newData = mapOf(
                                    "userID" to userID,
                                    "profileSetUp" to false
                                )

                                val userRegistrationID = db.collection("users")
                                    .document(userID)
                                    .collection("registration")
                                    .document(userID)
                                    .set(newData)
                                    .await()

                                    dataStoreManager.write(DataStoreKeys.loggedInProfile, "true")
                                    onNavigate(View.profileSetUp.name)
                            }
                }
                }
            } else {
                _errorAlertStateTitle.value = "Uh-oh"
                _errorAlertState.value =
                    "There was an error signing in: ${task.exception?.localizedMessage}"
            }
        }
    }

    fun forgotPasswordPressed() {
        onNavigate(View.forgotPassword.name)
    }
}
