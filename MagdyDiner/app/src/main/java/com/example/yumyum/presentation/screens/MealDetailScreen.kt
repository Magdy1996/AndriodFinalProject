package com.example.yumyum.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yumyum.presentation.orders.OrderViewModel
import com.example.yumyum.presentation.navigation.Screen
import com.example.yumyum.R
import com.example.yumyum.presentation.HeadingTextComponent
import com.example.yumyum.presentation.TextTitleMealInfo
import com.example.yumyum.presentation.meal_detail.MealDetailViewModel
import com.example.yumyum.presentation.meal_detail.components.MealDetailItem
import com.example.yumyum.presentation.meal_detail.components.MealIngredients
import com.example.yumyum.presentation.meal_detail.components.MealInstructions

/**
 * MealDetailScreen displays comprehensive information about a single meal.
 * It shows the meal image, ingredients, and cooking instructions.
 *
 * This screen demonstrates:
 * - Scrollable content for displaying large amounts of information
 * - Organizing complex layouts with Card components
 * - Showing nested composable components
 *
 * @param navController Used for navigation (back button)
 * @param viewModel The ViewModel that manages this screen's data (injected by Hilt)
 */
@Composable
fun MealDetailScreen(
    navController: NavController,
    viewModel: MealDetailViewModel = hiltViewModel()
) {
    // Collect the meal detail state from the view model
    val state by viewModel.state.collectAsState()
    // Hilt-injected ViewModel for orders
    val orderViewModel: OrderViewModel = hiltViewModel()
    // Local quantity state for ordering
    var quantity by remember { mutableStateOf(1) }
    val context = LocalContext.current

    // Observe order errors and show a Toast so the user sees failures instead of the app crashing
    val orderError by orderViewModel.error.collectAsState()
    LaunchedEffect(orderError) {
        orderError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            orderViewModel.clearError()
        }
    }

    // Navigate only after the order is confirmed to be placed/updated in the DB
    LaunchedEffect(key1 = orderViewModel) {
        orderViewModel.orderPlaced.collect { success ->
            if (success) {
                try {
                    navController.navigate(Screen.CartScreen.route)
                } catch (e: Exception) {
                    Toast.makeText(context, "Navigation failed: ${e.message}", Toast.LENGTH_LONG).show()
                    android.util.Log.e("MealDetailScreen", "Navigation to OrdersScreen failed", e)
                }
            }
        }
    }

    // Main container with loading and error states
    Box(Modifier.fillMaxSize()) {
        // Column with vertical scrolling for content that may exceed screen height
        Column(
            Modifier
                .fillMaxWidth()
                // verticalScroll() enables scrolling when content is taller than the screen
                .verticalScroll(rememberScrollState()),
        ) {
            // Header row with back button and title
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = CenterVertically
            ) {
                // Back button to return to meals list
                Icon(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = "back_icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(start = 5.dp, end = 10.dp, top = 10.dp)
                        .clip(CircleShape)
                        .size(30.dp)
                        .clickable(
                            onClick = {
                                navController.popBackStack()
                            }
                        )
                        .alignByBaseline()
                )
                // Display the screen title
                HeadingTextComponent(
                    text = "Meal Info",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            // Card is a Material Design component that provides elevation (shadow effect)
            // It groups related content and makes the UI more organized
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                colors = CardDefaults.cardColors(
                    // Set the card's background color to match the app theme
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    // Add shadow elevation for visual depth
                    defaultElevation = 10.dp
                )
            ) {
                // Display basic meal information (image, name, category, area)
                // firstOrNull() safely gets the first meal or null if list is empty
                state.meals.firstOrNull()?.let { meal ->
                    MealDetailItem(mealInfo = meal)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Quantity selector row
                    Row(modifier = Modifier.padding(8.dp)) {
                        IconButton(onClick = { if (quantity > 1) quantity -= 1 }) {
                            Icon(Icons.Default.Remove, contentDescription = "decrease")
                        }
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { quantity += 1 }) {
                            Icon(Icons.Default.Add, contentDescription = "increase")
                        }
                    }
                    // Order button: saves an order locally and navigates to orders screen
                    Button(onClick = {
                        // Validate meal identifiers before inserting to DB to avoid runtime errors
                        if (meal.idMeal.isNotBlank() && meal.strMeal.isNotBlank()) {
                            // Request upsert; navigation will happen when the ViewModel emits success
                            orderViewModel.addOrUpdateOrder(meal.idMeal, meal.strMeal, quantity)
                        } else {
                            Toast.makeText(context, "Invalid meal data, cannot place order", Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Text(text = "Order")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Display the "Ingredients" section title
                TextTitleMealInfo("Ingredients")
                // Display the list of ingredients with measurements
                state.meals.firstOrNull()?.let { meal ->
                    MealIngredients(mealInfo = meal)
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Display the "Instructions" section title
                TextTitleMealInfo("Instructions")
                // Display the cooking instructions
                state.meals.firstOrNull()?.let { meal ->
                    MealInstructions(mealInfo = meal)
                }
            }
        }
        // Display error message only if an error occurred during data loading
        if (state.error.isNotBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .align(Alignment.Center)
            )
        }
        // Display loading indicator while data is being fetched
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

}