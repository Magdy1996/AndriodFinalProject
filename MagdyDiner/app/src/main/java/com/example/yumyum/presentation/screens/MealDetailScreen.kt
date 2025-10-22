package com.example.yumyum.presentation.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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