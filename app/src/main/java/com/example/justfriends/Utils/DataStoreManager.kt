package com.example.justfriends.Utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth

val Context.dataStore by preferencesDataStore(name = "settings")
private lateinit var auth: FirebaseAuth

object DataStoreKeys {
    val email = stringPreferencesKey("email")
    val password = stringPreferencesKey("password")
    val profileSetUp = stringPreferencesKey("profileSetUp${auth.currentUser?.uid}")
    val loggedInHome = stringPreferencesKey("loggedInHome")
    val loggedInProfile = stringPreferencesKey("loggedInProfile")
}