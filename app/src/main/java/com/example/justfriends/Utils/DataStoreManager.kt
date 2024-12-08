package com.example.justfriends.Utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "settings")

object DataStoreKeys {
    val email = stringPreferencesKey("email")
    val password = stringPreferencesKey("password")
    val loggedInHome = stringPreferencesKey("loggedInHome")
    val loggedInProfile = stringPreferencesKey("loggedInProfile")
    val onChatView = stringPreferencesKey("onChatView")
    val distancePreference = stringPreferencesKey("distancePreference")
    val lastShareDate = stringPreferencesKey("lastShareDate")
}

class DataStoreManager(private val context: Context) {

    val dataStore = context.dataStore

    suspend fun read(key: Preferences.Key<String>): String {
           val value = dataStore.data.first().let { settings ->
                    settings[key] ?: ""
            }
        return value
    }

    suspend fun write(key: Preferences.Key<String>, value: String) {
        dataStore.edit { settings ->
            settings[key] = value
        }
    }
}