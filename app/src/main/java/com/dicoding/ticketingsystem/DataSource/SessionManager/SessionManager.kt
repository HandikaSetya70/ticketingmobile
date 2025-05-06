package com.dicoding.ticketingsystem.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class SessionManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val EMAIL = stringPreferencesKey("email")
        private val CREATED_AT = stringPreferencesKey("created_at")
        private val EXPIRES_IN = intPreferencesKey("expires_in")
        private val ID_NAME = stringPreferencesKey("id_name")
        private val VERIFICATION_STATUS = stringPreferencesKey("verification_status")
        private val ROLE = stringPreferencesKey("role")
    }

    suspend fun createLoginSession(
        userId: String,
        email: String,
        accessToken: String,
        refreshToken: String,
        createdAt: String,
        expiresIn: Int,
        idName: String?,
        verificationStatus: String?,
        role: String?
    ) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[EMAIL] = email
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[CREATED_AT] = createdAt
            preferences[EXPIRES_IN] = expiresIn
            idName?.let { preferences[ID_NAME] = it }
            verificationStatus?.let { preferences[VERIFICATION_STATUS] = it }
            role?.let { preferences[ROLE] = it }
        }
    }

    suspend fun clearSession() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val isLoggedIn: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    val accessToken: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val userId: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    val email: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[EMAIL]
        }

    val idName: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[ID_NAME]
        }

    val verificationStatus: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[VERIFICATION_STATUS]
        }

    val role: Flow<String?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[ROLE]
        }
}