package com.example.yumyum.presentation.orders

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.yumyum.util.PaymentValidators
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * CartScreen displays the user's pending (not submitted) orders as a shopping cart.
 * It allows updating quantities, removing items, viewing totals and submitting the cart.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val pending by viewModel.pendingOrders.collectAsState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Compute total items
    val totalItems = pending.fold(0) { acc, o -> acc + o.quantity }

    var showPaymentDialog by remember { mutableStateOf(false) }
    // Payment fields
    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }

    // Use shared validators (pure, testable)
    val isCardNumberValid = { num: String -> PaymentValidators.isCardNumberValid(num) }
    val isExpiryValid = { exp: String -> PaymentValidators.isExpiryValid(exp) }
    val isCvcValid = { code: String -> PaymentValidators.isCvcValid(code) }
    val isCardHolderValid = { name: String -> PaymentValidators.isCardHolderValid(name) }

    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Cart", style = MaterialTheme.typography.headlineSmall)
            Row {
                Button(onClick = { viewModel.clearAll() }) {
                    Text("Clear All")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = { navController.navigateUp() }) {
                    Text("Back")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (pending.isEmpty()) {
            Text(text = "Your cart is empty", modifier = Modifier.padding(8.dp))
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
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (order.quantity > 1) {
                                            viewModel.updateQuantity(order.id, order.quantity - 1)
                                        } else {
                                            viewModel.deleteOrder(order.id)
                                        }
                                    }) {
                                        Icon(Icons.Default.Remove, contentDescription = "decrease")
                                    }
                                    Text(text = order.quantity.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { viewModel.updateQuantity(order.id, order.quantity + 1) }) {
                                        Icon(Icons.Default.Add, contentDescription = "increase")
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteOrder(order.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Totals row and checkout
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total items: $totalItems", style = MaterialTheme.typography.titleMedium)
                Button(onClick = {
                    // show payment dialog instead of immediate submit
                    if (pending.isNotEmpty()) {
                        showPaymentDialog = true
                    }
                }) {
                    Text("Checkout")
                }
            }
        }
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Enter payment details") },
            text = {
                Column {
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = { cardHolder = it },
                        label = { Text("Cardholder name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it.filter { ch -> ch.isDigit() || ch == ' ' || ch == '-' } },
                        label = { Text("Card number") },
                        singleLine = true,
                        isError = cardNumber.isNotEmpty() && !isCardNumberValid(cardNumber)
                    )
                    // show helper/error text when the card number is invalid
                    if (cardNumber.isNotEmpty() && !isCardNumberValid(cardNumber)) {
                        Text(
                            text = "Card number must contain exactly 16 digits",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it.filter { ch -> ch.isDigit() || ch == '/' } },
                        label = { Text("Expiry (MM/YY)") },
                        singleLine = true,
                        isError = expiry.isNotEmpty() && !isExpiryValid(expiry)
                    )
                    if (expiry.isNotEmpty() && !isExpiryValid(expiry)) {
                        Text(
                            text = "Expiry must be MM/YY and year not after current year",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { cvc = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("CVC") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val valid = isCardHolderValid(cardHolder) && isCardNumberValid(cardNumber) && isExpiryValid(expiry) && isCvcValid(cvc)
                TextButton(onClick = {
                    // Extra guard: do nothing if validation failed (defense-in-depth)
                    if (!valid) return@TextButton

                    // perform submit
                    scope.launch {
                        try {
                            val ids = pending.map { it.id }
                            viewModel.submitOrdersByIds(ids)
                            Toast.makeText(context, "Order submitted", Toast.LENGTH_LONG).show()
                            showPaymentDialog = false
                            // clear sensitive fields
                            cardHolder = ""
                            cardNumber = ""
                            expiry = ""
                            cvc = ""
                            navController.navigate(com.example.yumyum.presentation.navigation.Screen.OrdersScreen.route)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Submit failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }, enabled = valid) {
                     Text("Confirm")
                 }
             },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("Cancel") }
            }
        )
    }
}
