package com.example.yumyum.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyum.domain.model.orders.Order
import com.example.yumyum.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    val orders: StateFlow<List<Order>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Expose pending (not submitted) orders
    val pendingOrders: StateFlow<List<Order>> = repository.getPendingOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Expose submitted orders
    val submittedOrders: StateFlow<List<Order>> = repository.getSubmittedOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Expose errors from order operations so UI can show them instead of crashing
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Emit an event when an order has been successfully placed/updated so the UI can react
    private val _orderPlaced = MutableSharedFlow<Boolean>(replay = 0)
    val orderPlaced = _orderPlaced.asSharedFlow()

    fun placeOrder(mealId: String, mealName: String, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                val order = Order(
                    mealId = mealId,
                    mealName = mealName,
                    quantity = quantity,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertOrder(order)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "placeOrder failed", e)
                _error.value = e.message ?: "Failed to place order"
            }
        }
    }

    // Upsert: add to cart. If order exists for same meal, increase quantity.
    fun addOrUpdateOrder(mealId: String, mealName: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val order = Order(
                    mealId = mealId,
                    mealName = mealName,
                    quantity = quantity,
                    timestamp = System.currentTimeMillis()
                )
                val result = repository.upsertOrder(order)
                // repository returns -1L on failure — emit success event only when >= 0
                if (result >= 0L) {
                    _orderPlaced.emit(true)
                } else {
                    _error.value = "Failed to add/update order"
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "addOrUpdateOrder failed", e)
                _error.value = "${e::class.java.simpleName}: ${e.message}"
            }
        }
    }

    // Update the quantity for an existing order id
    fun updateQuantity(id: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                repository.updateOrderQuantityById(id, quantity)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "updateQuantity failed", e)
                _error.value = e.message ?: "Failed to update quantity"
            }
        }
    }

    // Submit specific orders by their ids (mark as submitted in DB)
    fun submitOrdersByIds(ids: List<Long>) {
        viewModelScope.launch {
            try {
                repository.submitOrdersByIds(ids)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "submitOrdersByIds failed", e)
                _error.value = e.message ?: "Failed to submit orders"
            }
        }
    }

    // Submit all pending orders
    fun submitAllPending() {
        viewModelScope.launch {
            try {
                repository.submitAllPending()
            } catch (e: Exception) {
                Log.e("OrderViewModel", "submitAllPending failed", e)
                _error.value = e.message ?: "Failed to submit all pending orders"
            }
        }
    }

    fun deleteOrder(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteOrderById(id)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "deleteOrder failed", e)
                _error.value = e.message ?: "Failed to delete order"
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            try {
                repository.clearOrders()
            } catch (e: Exception) {
                Log.e("OrderViewModel", "clearAll failed", e)
                _error.value = e.message ?: "Failed to clear orders"
            }
        }
    }

    // Allow UI to clear the current error after showing it
    fun clearError() {
        _error.value = null
    }
}
