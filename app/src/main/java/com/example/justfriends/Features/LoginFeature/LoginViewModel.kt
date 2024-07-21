package com.example.justfriends.Features.LoginFeature

import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.datastore.preferences.core.edit
import android.app.Application
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.dataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginViewModel(justFriends: Application): AndroidViewModel(justFriends) {

    private val appContext = getApplication<Application>().applicationContext
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
            appContext.dataStore.data.collect { preferences ->
                userEmail.value = preferences[DataStoreKeys.email] ?: ""
                userPassword.value = preferences[DataStoreKeys.password] ?: ""
            }
        }
    }


    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }

    fun getUserPreference(key: String): String {
        var returnValue = ""
        viewModelScope.launch {
            appContext.dataStore.data.collect { preferences ->
                if (key == "password") {
                    returnValue = preferences[DataStoreKeys.password] ?: ""
                }
                if (key == "email") {
                    returnValue = preferences[DataStoreKeys.email] ?: ""
                }
                if (key == "profileSetUp") {
                    returnValue = preferences[DataStoreKeys.profileSetUp] ?: ""
                }
            }
        }
        return returnValue
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
                appContext.dataStore.edit { settings ->
                    settings[DataStoreKeys.email] = userEmail.value
                    settings[DataStoreKeys.password] = userPassword.value
                }
            }
            auth.createUserWithEmailAndPassword(userEmail.value, userPassword.value)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                        login(email = userEmail.value, password = userPassword.value)
                    }
                    } else {
                        _errorAlertStateTitle.value = "Uh-oh"
                        _errorAlertState.value = "There was an error registering: ${task.exception?.localizedMessage}"
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
                            appContext.dataStore.edit { settings ->
                                settings[DataStoreKeys.email] = userEmail.value
                                settings[DataStoreKeys.password] = userPassword.value
                            }
                        }

                        if (getUserPreference(key = "profileSetUp") != "") {
                            if (getUserPreference(key = "profileSetUp") == "true") {
                                viewModelScope.launch {
                                    appContext.dataStore.edit { settings ->
                                        settings[DataStoreKeys.loggedInHome] = "true"
                                    }
                                }
                                onNavigate(View.home.name)
                            } else {
                                viewModelScope.launch {
                                    appContext.dataStore.edit { settings ->
                                        settings[DataStoreKeys.loggedInProfile] = "true"
                                    }
                                }
                               onNavigate(View.profileSetUp.name)
                            }
                        } else {
                            viewModelScope.launch {
                            val userCollection = db.collection("users")
                                .document(userID)
                                .collection("registration")
                                .document(userID)
                                .get()
                                .await()

                                if (userCollection.exists()) {
                                    val data = userCollection.data
                                    if (data != null) {
                                        if (data["profileSetUp"] ?: "false" == "true") {
                                            viewModelScope.launch {
                                                appContext.dataStore.edit { settings ->
                                                    settings[DataStoreKeys.loggedInHome] = "true"
                                                }
                                            }
                                            onNavigate(View.home.name)
                                        } else {
                                            viewModelScope.launch {
                                                appContext.dataStore.edit { settings ->
                                                    settings[DataStoreKeys.loggedInProfile] = "true"
                                                }
                                            }
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

                                    viewModelScope.launch {
                                        appContext.dataStore.edit { settings ->
                                            settings[DataStoreKeys.profileSetUp] = "false"
                                            settings[DataStoreKeys.loggedInProfile] = "true"
                                        }
                                        onNavigate(View.profileSetUp.name)
                                    }
                                }
                            }

                        }

                    }

                } else {
                    _errorAlertStateTitle.value = "Uh-oh"
                    _errorAlertState.value = "There was an error signing in: ${task.exception?.localizedMessage}"
                }
        }
    }

    fun forgotPasswordPressed() {
        onNavigate(View.forgotPassword.name)
    }
}