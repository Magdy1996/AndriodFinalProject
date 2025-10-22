package com.example.yumyum.presentation.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yumyum.R
import com.example.yumyum.presentation.HeadingTextComponent
import com.example.yumyum.presentation.category_list.CategoryListViewModel
import com.example.yumyum.presentation.category_list.components.SingleCategoryItem
import com.example.yumyum.presentation.navigation.Screen

/**
 * CategoriesScreen displays a list of all available meal categories.
 * Users can tap on a category to see all meals in that category.
 *
 * This screen demonstrates:
 * - ViewModel usage for managing state
 * - Flow/StateFlow for reactive data updates
 * - LazyColumn for displaying scrollable lists
 * - Error and loading state handling
 *
 * @param onCategoryClick Callback function called when user selects a category
 * @param viewModel The ViewModel that manages this screen's data and state (injected by Hilt)
 */
@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    // collectAsState() converts a Flow to a Compose State
    // This allows the UI to automatically recompose when the state changes
    // It's a bridge between reactive data flows and Compose's declarative UI
    val state by viewModel.state.collectAsState()

    // Main container with loading and error states
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            // Header row with title
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = CenterVertically,
            ) {
                // Display the screen title using a reusable component
                HeadingTextComponent(
                    text = "Categories",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                        .weight(1f)
                )
            }
            // LazyColumn is like RecyclerView - it only renders visible items for performance
            // This is important for long lists as it reduces memory usage
            LazyColumn(
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier.fillMaxWidth()
            ){
                // items() iterates through the categories list and renders each one
                // If the list changes, Compose automatically updates the UI
                items(state.categories) { category ->
                    SingleCategoryItem(
                        categoryItem = category,
                        // Pass the callback to handle when user taps this category
                        onCategoryItemClick = onCategoryClick
                    )
                }
            }
        }
        // Display error message only if an error occurred AND the string is not blank
        // This prevents showing empty error states
        if (state.error.isNotBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    // align() positions this Text in the center of the Box
                    .align(Alignment.Center)
            )
        }
        // Display loading indicator only while data is being fetched
        // Once loading completes, this automatically disappears
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

}



