package com.example.yumyum.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mealId: String,
    val mealName: String,
    val quantity: Int,
    val timestamp: Long,
    // Whether this order has been submitted (finalized) by the user
    val isSubmitted: Boolean = false,
    // Associate orders with a local user; 0 means guest (no user)
    val userId: Long = 0L
)
