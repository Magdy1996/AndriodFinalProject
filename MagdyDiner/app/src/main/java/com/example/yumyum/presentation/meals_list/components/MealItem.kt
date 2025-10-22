package com.example.yumyum.presentation.meals_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yumyum.domain.model.meals.Meals

/**
 * SingleMealItem displays a single meal item in the meals list.
 * It shows the meal thumbnail image on the left and the meal name on the right.
 *
 * This component is rendered by LazyColumn for each meal in the list.
 *
 * @param mealsItem The meal data to display
 * @param onMealItemClick Callback function called when user taps this meal
 */
@Composable
fun SingleMealItem(
    mealsItem: Meals,
    onMealItemClick: (String) -> Unit
) {
    // Card provides a material design container with elevation
    Card(
        Modifier
            .padding(10.dp)
            .fillMaxWidth()
            // heightIn() sets a maximum height to keep items consistently sized
            .heightIn(max = 140.dp)
            // Make the entire card clickable
            .clickable { onMealItemClick(mealsItem.idMeal) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        // Row arranges the image and text horizontally
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Align items vertically to the center
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container with fixed size for the meal thumbnail
            Box(modifier = Modifier.size(140.dp)) {
                // AsyncImage loads the meal image asynchronously
                // This prevents the UI from freezing while downloading the image
                AsyncImage(
                    model = mealsItem.strMealThumb,
                    contentDescription = "dish-image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            // Display the meal name
            Text(
                text = mealsItem.strMeal,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                // Truncate with "..." if the name is too long
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            )
        }
    }
}