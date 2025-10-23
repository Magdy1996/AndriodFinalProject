package com.example.yumyum.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val email: String,
    // Optional username for credential-based login (nullable for backwards compatibility)
    val username: String? = null,
    // Stored password hash (nullable: some legacy or placeholder users may not have credentials)
    val passwordHash: String? = null,
    val displayName: String? = null,
    // New fields collected at signup
    val phoneNumber: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
