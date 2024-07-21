package com.example.justfriends.Utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

val Context.dataStore by preferencesDataStore(name = "settings")
private var auth = Firebase.auth

object DataStoreKeys {
    val email = stringPreferencesKey("email")
    val password = stringPreferencesKey("password")
    val profileSetUp = stringPreferencesKey("profileSetUp${auth.currentUser?.uid}")
    val loggedInHome = stringPreferencesKey("loggedInHome")
    val loggedInProfile = stringPreferencesKey("loggedInProfile")
}