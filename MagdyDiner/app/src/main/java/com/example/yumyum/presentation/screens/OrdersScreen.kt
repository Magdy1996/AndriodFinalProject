package com.example.yumyum.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yumyum.presentation.navigation.Screen
import com.example.yumyum.presentation.orders.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    // Use pending and submitted flows
    val pending by viewModel.pendingOrders.collectAsState()
    val submitted by viewModel.submittedOrders.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Your Orders", style = MaterialTheme.typography.headlineSmall)
            Row {
                Button(onClick = { viewModel.clearAll() }) {
                    Text("Clear All")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = {
                    // submit all pending orders
                    scope.launch { viewModel.submitAllPending() }
                }, enabled = pending.isNotEmpty()) {
                    Text("Submit All")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                    Text("Switch User")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (pending.isEmpty()) {
            Text(text = "No pending orders", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn {
                items(pending) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        colors = CardDefaults.cardColors()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = order.mealName, style = MaterialTheme.typography.titleMedium)
                                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                Text(text = sdf.format(Date(order.timestamp)), style = MaterialTheme.typography.bodySmall)
                                // Quantity controls: decrease / quantity / increase
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (order.quantity > 1) {
                                            viewModel.updateQuantity(order.id, order.quantity - 1)
                                        } else {
                                            viewModel.deleteOrder(order.id)
                                        }
                                    }) {
                                        Icon(Icons.Filled.Remove, contentDescription = "decrease")
                                    }
                                    Text(text = order.quantity.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { viewModel.updateQuantity(order.id, order.quantity + 1) }) {
                                        Icon(Icons.Filled.Add, contentDescription = "increase")
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteOrder(order.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // Submitted orders section
        Text(text = "Submitted Orders", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (submitted.isEmpty()) {
            Text(text = "No submitted orders yet", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn {
                items(submitted) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        colors = CardDefaults.cardColors()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = order.mealName, style = MaterialTheme.typography.titleMedium)
                                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                Text(text = sdf.format(Date(order.timestamp)), style = MaterialTheme.typography.bodySmall)
                                Text(text = "Quantity: ${order.quantity}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
