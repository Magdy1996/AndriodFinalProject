package com.example.yumyum.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.yumyum.data.local.AppDatabase
import com.example.yumyum.data.local.OrderDao
import com.example.yumyum.data.local.entities.OrderEntity
import com.example.yumyum.domain.model.orders.Order
import com.example.yumyum.domain.repository.OrderRepository
import com.example.yumyum.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class OrderRepositoryImpl @Inject constructor(
    private val dao: OrderDao,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
): OrderRepository {

    // Lazily-created in-memory fallback DAO in case the disk DB is closed/corrupt
    private var fallbackDao: OrderDao? = null

    private fun ensureFallbackDao(): OrderDao {
        return fallbackDao ?: run {
            val fallbackDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
            val d = fallbackDb.orderDao()
            fallbackDao = d
            Log.w("OrderRepo", "Using in-memory fallback DB due to closed or corrupt disk DB")
            d
        }
    }

    // Helper to detect corruption-like messages
    private fun String?.indicatesCorruptionOrClosed(): Boolean {
        val msg = this?.lowercase() ?: return false
        return msg.contains("closed") || msg.contains("verify the data integrity") || msg.contains("disk image is malformed") || msg.contains("malformed")
    }

    override suspend fun insertOrder(order: Order): Long = withContext(Dispatchers.IO) {
        try {
            val uid = authRepository.getCurrentUserId()
            val entity = OrderEntity(
                mealId = order.mealId,
                mealName = order.mealName,
                quantity = order.quantity,
                timestamp = order.timestamp,
                isSubmitted = order.isSubmitted,
                userId = uid
            )
            dao.insert(entity)
        } catch (e: IllegalStateException) {
            // Detect closed or corrupted DB and retry against an in-memory fallback
            if (e.message.indicatesCorruptionOrClosed()) {
                Log.e("OrderRepo", "insertOrder: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                val uid = authRepository.getCurrentUserId()
                return@withContext ensureFallbackDao().insert(OrderEntity(
                    mealId = order.mealId,
                    mealName = order.mealName,
                    quantity = order.quantity,
                    timestamp = order.timestamp,
                    isSubmitted = order.isSubmitted,
                    userId = uid
                ))
            }
            Log.e("OrderRepo", "insertOrder failed", e)
            -1L
        } catch (e: Exception) {
            Log.e("OrderRepo", "insertOrder failed", e)
            -1L
        }
    }

    override fun getOrders(): Flow<List<Order>> {
        return flow {
            val uid = authRepository.getCurrentUserId()
            emitAll(
                dao.getAllOrders(uid).map { list ->
                    list.map { e ->
                        Order(
                            id = e.id,
                            mealId = e.mealId,
                            mealName = e.mealName,
                            quantity = e.quantity,
                            timestamp = e.timestamp,
                            isSubmitted = e.isSubmitted,
                            userId = e.userId
                        )
                    }
                }
            )
        }.catch { e ->
            Log.e("OrderRepo", "getOrders flow failed", e)
            emit(emptyList())
        }
    }

    override fun getPendingOrders(): Flow<List<Order>> {
        return flow {
            val uid = authRepository.getCurrentUserId()
            emitAll(
                dao.getPendingOrders(uid).map { list ->
                    list.map { e ->
                        Order(
                            id = e.id,
                            mealId = e.mealId,
                            mealName = e.mealName,
                            quantity = e.quantity,
                            timestamp = e.timestamp,
                            isSubmitted = e.isSubmitted,
                            userId = e.userId
                        )
                    }
                }
            )
        }.catch { e ->
            Log.e("OrderRepo", "getPendingOrders flow failed", e)
            emit(emptyList())
        }
    }

    override fun getSubmittedOrders(): Flow<List<Order>> {
        return flow {
            val uid = authRepository.getCurrentUserId()
            emitAll(
                dao.getSubmittedOrders(uid).map { list ->
                    list.map { e ->
                        Order(
                            id = e.id,
                            mealId = e.mealId,
                            mealName = e.mealName,
                            quantity = e.quantity,
                            timestamp = e.timestamp,
                            isSubmitted = e.isSubmitted,
                            userId = e.userId
                        )
                    }
                }
            )
        }.catch { e ->
            Log.e("OrderRepo", "getSubmittedOrders flow failed", e)
            emit(emptyList())
        }
    }

    // Replace expression-body implementation with explicit Unit-returning block to match interface
    override suspend fun deleteOrderById(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                val uid = authRepository.getCurrentUserId()
                dao.deleteById(id, uid)
            } catch (e: IllegalStateException) {
                if (e.message.indicatesCorruptionOrClosed()) {
                    Log.w("OrderRepo", "deleteOrderById: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                    val uid = authRepository.getCurrentUserId()
                    ensureFallbackDao().deleteById(id, uid)
                } else {
                    Log.e("OrderRepo", "deleteOrderById failed", e)
                }
            } catch (e: Exception) {
                Log.e("OrderRepo", "deleteOrderById failed", e)
            }
        }
    }

    override suspend fun clearOrders() {
        withContext(Dispatchers.IO) {
            try {
                val uid = authRepository.getCurrentUserId()
                dao.deleteAll(uid)
            } catch (e: IllegalStateException) {
                if (e.message.indicatesCorruptionOrClosed()) {
                    Log.w("OrderRepo", "clearOrders: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                    val uid = authRepository.getCurrentUserId()
                    ensureFallbackDao().deleteAll(uid)
                } else {
                    Log.e("OrderRepo", "clearOrders failed", e)
                }
            } catch (e: Exception) {
                Log.e("OrderRepo", "clearOrders failed", e)
            }
        }
    }

    override suspend fun upsertOrder(order: Order): Long = withContext(Dispatchers.IO) {
        try {
            // If an order exists for the meal, increase its quantity and update timestamp
            val uid = authRepository.getCurrentUserId()
            val existing = dao.getOrderByMealId(order.mealId, uid)
            Log.d("OrderRepo", "upsertOrder called for mealId=${order.mealId}, existing=${existing != null}")
            val result: Long = if (existing != null) {
                val newQuantity = existing.quantity + order.quantity
                Log.d("OrderRepo", "Updating existing order id=${existing.id} newQuantity=$newQuantity")
                dao.updateQuantityById(existing.id, newQuantity, System.currentTimeMillis(), uid)
                existing.id
            } else {
                val entity = OrderEntity(
                    mealId = order.mealId,
                    mealName = order.mealName,
                    quantity = order.quantity,
                    timestamp = order.timestamp,
                    isSubmitted = order.isSubmitted,
                    userId = uid
                )
                val id = dao.insert(entity)
                Log.d("OrderRepo", "Inserted new order id=$id for mealId=${order.mealId}")
                id
            }
            return@withContext result
        } catch (e: IllegalStateException) {
            if (e.message.indicatesCorruptionOrClosed()) {
                Log.w("OrderRepo", "upsertOrder: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                try {
                    // Retry logic against in-memory fallback
                    val fDao = ensureFallbackDao()
                    val uid2 = authRepository.getCurrentUserId()
                    val existing = fDao.getOrderByMealId(order.mealId, uid2)
                    return@withContext if (existing != null) {
                        val newQuantity = existing.quantity + order.quantity
                        fDao.updateQuantityById(existing.id, newQuantity, System.currentTimeMillis(), uid2)
                        existing.id
                    } else {
                        val id = fDao.insert(OrderEntity(
                            mealId = order.mealId,
                            mealName = order.mealName,
                            quantity = order.quantity,
                            timestamp = order.timestamp,
                            isSubmitted = order.isSubmitted,
                            userId = uid2
                        ))
                        id
                    }
                } catch (inner: Exception) {
                    Log.e("OrderRepo", "upsertOrder fallback failed", inner)
                    throw inner
                }
            }
            Log.e("OrderRepo", "upsertOrder failed", e)
            throw e
        } catch (e: Exception) {
            Log.e("OrderRepo", "upsertOrder failed", e)
            throw e
        }
    }

    override suspend fun updateOrderQuantityById(id: Long, quantity: Int) {
        withContext(Dispatchers.IO) {
            try {
                val uid = authRepository.getCurrentUserId()
                dao.updateQuantityById(id, quantity, System.currentTimeMillis(), uid)
            } catch (e: IllegalStateException) {
                if (e.message.indicatesCorruptionOrClosed()) {
                    Log.w("OrderRepo", "updateOrderQuantityById: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                    val uid = authRepository.getCurrentUserId()
                    ensureFallbackDao().updateQuantityById(id, quantity, System.currentTimeMillis(), uid)
                } else {
                    Log.e("OrderRepo", "updateOrderQuantityById failed", e)
                }
            } catch (e: Exception) {
                Log.e("OrderRepo", "updateOrderQuantityById failed", e)
                // swallow
            }
        }
    }

    override suspend fun getOrderByMealId(mealId: String): Order? = withContext(Dispatchers.IO) {
        try {
            val uid = authRepository.getCurrentUserId()
            val ent = dao.getOrderByMealId(mealId, uid)
            if (ent != null) {
                return@withContext Order(
                    id = ent.id,
                    mealId = ent.mealId,
                    mealName = ent.mealName,
                    quantity = ent.quantity,
                    timestamp = ent.timestamp,
                    isSubmitted = ent.isSubmitted,
                    userId = ent.userId
                )
            }
            null
        } catch (e: IllegalStateException) {
            if (e.message.indicatesCorruptionOrClosed()) {
                Log.w("OrderRepo", "getOrderByMealId: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                val uid = authRepository.getCurrentUserId()
                val ent = ensureFallbackDao().getOrderByMealId(mealId, uid)
                if (ent != null) {
                    return@withContext Order(
                        id = ent.id,
                        mealId = ent.mealId,
                        mealName = ent.mealName,
                        quantity = ent.quantity,
                        timestamp = ent.timestamp,
                        isSubmitted = ent.isSubmitted,
                        userId = ent.userId
                    )
                }
                null
            } else {
                Log.e("OrderRepo", "getOrderByMealId failed", e)
                null
            }
        } catch (e: Exception) {
            Log.e("OrderRepo", "getOrderByMealId failed", e)
            null
        }
    }

    override suspend fun submitOrdersByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        try {
            if (ids.isNotEmpty()) {
                val uid = authRepository.getCurrentUserId()
                dao.markSubmittedByIds(ids, uid)
            } else Unit
        } catch (e: IllegalStateException) {
            if (e.message.indicatesCorruptionOrClosed()) {
                Log.w("OrderRepo", "submitOrdersByIds: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                val uid = authRepository.getCurrentUserId()
                ensureFallbackDao().markSubmittedByIds(ids, uid)
            } else {
                Log.e("OrderRepo", "submitOrdersByIds failed", e)
            }
        } catch (e: Exception) {
            Log.e("OrderRepo", "submitOrdersByIds failed", e)
            // swallow
            Unit
        }
    }

    override suspend fun submitAllPending() {
        withContext(Dispatchers.IO) {
            try {
                val uid = authRepository.getCurrentUserId()
                dao.markAllSubmitted(uid)
            } catch (e: IllegalStateException) {
                if (e.message.indicatesCorruptionOrClosed()) {
                    Log.w("OrderRepo", "submitAllPending: disk DB closed/corrupt, switching to in-memory fallback (will NOT delete disk DB)", e)
                    val uid = authRepository.getCurrentUserId()
                    ensureFallbackDao().markAllSubmitted(uid)
                } else {
                    Log.e("OrderRepo", "submitAllPending failed", e)
                }
            } catch (e: Exception) {
                Log.e("OrderRepo", "submitAllPending failed", e)
                // swallow
            }
        }
    }
}
