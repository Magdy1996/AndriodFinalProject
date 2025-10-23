package com.example.yumyum.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.yumyum.data.local.entities.OrderEntity

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: OrderEntity): Long

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllOrders(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isSubmitted = 0 AND userId = :userId ORDER BY timestamp DESC")
    fun getPendingOrders(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isSubmitted = 1 AND userId = :userId ORDER BY timestamp DESC")
    fun getSubmittedOrders(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getOrderById(id: Long, userId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE mealId = :mealId AND userId = :userId LIMIT 1")
    suspend fun getOrderByMealId(mealId: String, userId: Long): OrderEntity?

    @Update
    suspend fun update(order: OrderEntity)

    @Query("UPDATE orders SET quantity = :quantity, timestamp = :timestamp WHERE id = :id AND userId = :userId")
    suspend fun updateQuantityById(id: Long, quantity: Int, timestamp: Long, userId: Long)

    @Query("UPDATE orders SET isSubmitted = 1 WHERE id IN (:ids) AND userId = :userId")
    suspend fun markSubmittedByIds(ids: List<Long>, userId: Long)

    @Query("UPDATE orders SET isSubmitted = 1 WHERE userId = :userId")
    suspend fun markAllSubmitted(userId: Long)

    @Query("DELETE FROM orders WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: Long, userId: Long)

    @Query("DELETE FROM orders WHERE userId = :userId")
    suspend fun deleteAll(userId: Long)
}
