package com.example.justfriends.Features.ForgotPasswordFeature

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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ForgotPasswordViewModel(justFriends: Application,
                        private val dataStoreManager: DataStoreManager,
                        private val currentView: MutableState<String>,
                        private val navBarTitle: MutableState<String>,
                        private val shouldShowFAB: MutableState<Boolean>
): AndroidViewModel(justFriends) {

    private lateinit var auth: FirebaseAuth
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()
    var showError = mutableStateOf(false)
    var errorMessage = mutableStateOf("")
    var email = mutableStateOf("")

    fun sendLink() {
        if (!email.value.isEmpty()) {
            auth = Firebase.auth
            auth.sendPasswordResetEmail(email.value)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        errorMessage.value = task.exception?.localizedMessage ?: ""
                        showError.value = true
                    } else {
                        email.value = ""
                        showError.value = false
                    }
                }
        } else {
            errorMessage.value = "Please enter your email address in a valid format."
            showError.value = true
        }
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}