package com.example.yumyum.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyum.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exposes simple DataStore-backed user preferences (theme, login state, userId) to the UI.
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = prefs.isLoggedIn()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentTheme: StateFlow<String> = prefs.currentTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "light")

    val currentUserId: StateFlow<String?> = prefs.currentUserId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // New: expose last generated message from GenAI (saved in DataStore)
    val currentGeneratedMessage: StateFlow<String?> = prefs.currentGeneratedMessage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setTheme(theme: String) {
        viewModelScope.launch { prefs.setTheme(theme) }
    }

    fun setLoggedIn(loggedIn: Boolean, userId: String? = null) {
        viewModelScope.launch { prefs.setLoggedIn(loggedIn, userId) }
    }
}
