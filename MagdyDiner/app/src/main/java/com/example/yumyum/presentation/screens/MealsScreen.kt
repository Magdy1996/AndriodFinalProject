package com.example.yumyum.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.yumyum.presentation.meals_list.MealsListViewModel
import com.example.yumyum.presentation.meals_list.components.SingleMealItem

/**
 * MealsScreen displays a list of meals for a selected category.
 * Users can scroll through meals and tap on one to see detailed information.
 *
 * This screen demonstrates:
 * - Receiving parameters from navigation (the category name)
 * - Back navigation to the previous screen
 * - Responding to user interactions with callbacks
 *
 * @param onMealItemClick Callback function called when user selects a meal
 * @param navController Used for navigation between screens
 * @param viewModel The ViewModel that manages this screen's data (injected by Hilt)
 */
@Composable
fun MealsScreen(
    onMealItemClick: (String) -> Unit,
    navController: NavController,
    viewModel: MealsListViewModel = hiltViewModel()
) {
    // Collect the meals list state from the view model
    // This state automatically updates when new data is fetched from the API
    val state by viewModel.state.collectAsState()

    // Main container with loading and error states
    Box(Modifier.fillMaxSize()){
        Column(Modifier.fillMaxWidth()) {
            // Header row with back button and title
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = CenterVertically
            ) {
                // Back button icon that returns to the previous screen
                Icon(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = "back_icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(start = 5.dp, end = 10.dp, top = 10.dp)
                        // CircleShape makes the icon appear in a circular background
                        .clip(CircleShape)
                        .size(30.dp)
                        // clickable() makes the icon respond to taps
                        .clickable(
                            onClick = {
                                // popBackStack() removes this screen and shows the previous one
                                navController.popBackStack()
                            }
                        )
                        .alignByBaseline()
                )
                // Display the screen title
                HeadingTextComponent(
                    text = "Meals",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            // LazyColumn displays the meals in a scrollable list
            // Only visible items are rendered, making scrolling smooth even with many meals
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(10.dp)
            ) {
                // For each meal in the list, create a SingleMealItem composable
                items(state.meals) { dishes ->
                    SingleMealItem(
                        mealsItem = dishes,
                        // Pass the callback to handle when user taps this meal
                        onMealItemClick = onMealItemClick
                    )
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
                    .align(Alignment.Center)
            )
        }
        // Display loading indicator while data is being fetched
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

}