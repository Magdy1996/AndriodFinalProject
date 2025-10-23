package com.example.yumyum.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyum.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow(0L)
    val currentUserId: StateFlow<Long> = _currentUserId

    // Expose a display name (or username fallback) for the currently-signed-in user
    private val _currentUserDisplayName = MutableStateFlow<String?>(null)
    @Suppress("unused")
    val currentUserDisplayName: StateFlow<String?> = _currentUserDisplayName

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    // When sign-in fails but the username exists, we ask UI to show the change-password dialog
    data class PasswordUpdateRequest(val username: String)
    private val _passwordUpdateRequest = MutableStateFlow<PasswordUpdateRequest?>(null)
    val passwordUpdateRequest: StateFlow<PasswordUpdateRequest?> = _passwordUpdateRequest

    init {
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "Initializing - fetching current user ID")
                val id = authRepository.getCurrentUserId()
                _currentUserId.value = id
                android.util.Log.d("LoginViewModel", "Current user ID: ${_currentUserId.value}")
                // refresh display name for this user (if any)
                refreshCurrentUserDisplayName(id)
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error initializing current user", e)
                _currentUserId.value = 0L
                _currentUserDisplayName.value = null
            }
        }
    }

    private fun refreshCurrentUserDisplayName(id: Long) {
        viewModelScope.launch {
            try {
                if (id == 0L) {
                    _currentUserDisplayName.value = null
                    return@launch
                }
                val user = try { authRepository.getUserById(id) } catch (e: Exception) {
                    android.util.Log.e("LoginViewModel", "getUserById failed", e)
                    null
                }
                _currentUserDisplayName.value = when {
                    user == null -> null
                    !user.displayName.isNullOrBlank() -> user.displayName
                    !user.username.isNullOrBlank() -> user.username
                    else -> "User ${user.id}"
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "refreshCurrentUserDisplayName exception", e)
                _currentUserDisplayName.value = null
            }
        }
    }

    @Suppress("unused")
    fun setCurrentUserId(id: Long) {
        viewModelScope.launch {
            authRepository.setCurrentUserId(id)
            _currentUserId.value = id
            _statusMessage.value = "Switched to user $id"
            refreshCurrentUserDisplayName(id)
        }
    }

    // Updated signUp signature to accept email, phone and address
    fun signUp(username: String, password: String, displayName: String?, email: String, phoneNumber: String?, address: String?) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "signUp called for username: $username, email: $email")
                val id = authRepository.signUp(username.trim(), password, displayName?.trim(), email.trim(), phoneNumber?.trim(), address?.trim())
                android.util.Log.d("LoginViewModel", "signUp returned id: $id")
                if (id > 0) {
                    // Do NOT auto-set current user here. Instead, prompt user to login.
                    _statusMessage.value = "Signed up successfully (id=$id). Please login."
                } else {
                    _statusMessage.value = "Sign up failed (username or email may already exist)"
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "signUp exception", e)
                _statusMessage.value = "Sign up failed: ${e.message}"
            }
        }
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "signIn called for username: $username")
                val id = authRepository.signIn(username.trim(), password)
                android.util.Log.d("LoginViewModel", "signIn returned id: $id")
                if (id > 0) {
                    _currentUserId.value = id
                    _statusMessage.value = "Signed in as $username (id=$id)"
                    // refresh display name for the signed-in user
                    refreshCurrentUserDisplayName(id)
                    // clear any pending password update requests
                    _passwordUpdateRequest.value = null
                } else {
                    // Check whether username exists - if so offer password update dialog
                    val exists = try {
                        authRepository.usernameExists(username.trim())
                    } catch (e: Exception) {
                        android.util.Log.e("LoginViewModel", "usernameExists check failed", e)
                        false
                    }
                    if (exists) {
                        _statusMessage.value = "Wrong password for $username — would you like to update it?"
                        _passwordUpdateRequest.value = PasswordUpdateRequest(username.trim())
                    } else {
                        _statusMessage.value = "No account found for $username"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "signIn exception", e)
                _statusMessage.value = "Login failed: ${e.message}"
            }
        }
    }

    /**
     * Attempt to change the password for the given username (verifies oldPassword).
     * Emits status messages and clears the passwordUpdateRequest on success/failure.
     */
    fun updatePassword(username: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "updatePassword for username=$username")
                val ok = authRepository.updatePassword(username.trim(), oldPassword, newPassword)
                if (ok) {
                    _statusMessage.value = "Password updated successfully. Please login with your new password."
                    _passwordUpdateRequest.value = null
                } else {
                    _statusMessage.value = "Password update failed (old password incorrect or user missing)."
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "updatePassword exception", e)
                _statusMessage.value = "Password update failed: ${e.message}"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUserId.value = 0L
            _currentUserDisplayName.value = null
            _statusMessage.value = "Signed out"
        }
    }
}
