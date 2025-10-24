package com.example.yumyum.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Simple DataStore wrapper for user preferences.
 * Stores theme (light/dark) and user login state and userId.
 */
class UserPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        val THEME_KEY = stringPreferencesKey("pref_theme")
        val LOGGED_IN_KEY = booleanPreferencesKey("pref_logged_in")
        val USER_ID_KEY = stringPreferencesKey("pref_user_id")
        // New key to store the last generated GenAI message
        val GENERATED_MESSAGE_KEY = stringPreferencesKey("pref_generated_message")

        // Helper to obtain DataStore instance from a Context: prefer using DI in real apps
        fun getInstance(context: Context): UserPreferences {
            return UserPreferences(context.dataStore)
        }
    }

    fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_KEY] ?: false
    }

    fun currentTheme(): Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "light"
    }

    fun currentUserId(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    // New: expose last generated message as a Flow
    fun currentGeneratedMessage(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[GENERATED_MESSAGE_KEY]
    }

    suspend fun setLoggedIn(loggedIn: Boolean, userId: String? = null) {
        dataStore.edit { prefs ->
            prefs[LOGGED_IN_KEY] = loggedIn
            if (userId != null) prefs[USER_ID_KEY] = userId
        }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    // New: persist the generated message
    suspend fun setGeneratedMessage(message: String) {
        dataStore.edit { prefs ->
            prefs[GENERATED_MESSAGE_KEY] = message
        }
    }

}
