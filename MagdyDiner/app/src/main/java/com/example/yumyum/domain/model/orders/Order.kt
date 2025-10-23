package com.example.yumyum.domain.model.orders

/**
 * Domain model representing an Order placed by the user.
 */
data class Order(
    val id: Long = 0L,
    val mealId: String,
    val mealName: String,
    val quantity: Int,
    val timestamp: Long,
    val isSubmitted: Boolean = false,
    val userId: Long = 0L
)
