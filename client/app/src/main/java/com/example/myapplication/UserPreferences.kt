package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class UserPreferences(context: Context) {
    private val dataStore: DataStore<Preferences> = context.userDataStore

    companion object {
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_CREATED_AT = stringPreferencesKey("created_at")
        private val KEY_TOKEN = stringPreferencesKey("token")
    }

    suspend fun saveUserData(id: Int, username: String, email: String, createdAt: String, token: String? = null) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
            preferences[KEY_USERNAME] = username
            preferences[KEY_EMAIL] = email
            preferences[KEY_CREATED_AT] = createdAt
            if (token != null) {
                preferences[KEY_TOKEN] = token
            }
        }
    }

    val userId: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }

    val username: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USERNAME]
    }

    val email: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_EMAIL]
    }

    val createdAt: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_CREATED_AT]
    }

    val token: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_TOKEN]
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}