package com.example.yumyum.domain.repository

import com.example.yumyum.domain.model.orders.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun insertOrder(order: Order): Long
    fun getOrders(): Flow<List<Order>>
    fun getPendingOrders(): Flow<List<Order>>
    fun getSubmittedOrders(): Flow<List<Order>>
    suspend fun deleteOrderById(id: Long)
    suspend fun clearOrders(): Any

    // Upsert an order: if an order exists for the same mealId, add quantities and update timestamp
    suspend fun upsertOrder(order: Order): Long
    // Update the quantity of an existing order by id
    suspend fun updateOrderQuantityById(id: Long, quantity: Int): Any

    // Find an order by meal id (optional helper)
    suspend fun getOrderByMealId(mealId: String): Order?
    // Mark orders as submitted
    suspend fun submitOrdersByIds(ids: List<Long>): Any
    suspend fun submitAllPending(): Any
}
