package com.example.yumyum.auth

import com.example.yumyum.data.local.entities.UserEntity

/**
 * Simple abstraction for an authentication store.
 * This implementation is intentionally minimal: it tracks a local currentUserId (Long), 0 means guest.
 *
 * Note: signUp and signIn use username + password credentials for this app. signUp collects profile info.
 */
interface AuthRepository {
    suspend fun getCurrentUserId(): Long
    suspend fun setCurrentUserId(id: Long)

    /**
     * Create a new local user with the given username and password and profile info. Returns the new user id.
     */
    suspend fun signUp(username: String, password: String, displayName: String?, email: String, phoneNumber: String?, address: String?): Long

    /**
     * Sign in an existing user by username and password. Returns the user id or 0 if not found or password mismatch.
     */
    suspend fun signIn(username: String, password: String): Long

    /**
     * Sign out the current user (set to guest/0)
     */
    suspend fun signOut()

    /**
     * Return user details by id or null if not found
     */
    suspend fun getUserById(id: Long): UserEntity?

    /**
     * Return true if a user with this username exists (regardless of password)
     */
    suspend fun usernameExists(username: String): Boolean

    /**
     * Update a user's password given their username and old password. Returns true if update succeeded.
     */
    suspend fun updatePassword(username: String, oldPassword: String, newPassword: String): Boolean
}
