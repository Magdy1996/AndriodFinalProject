package com.example.yumyum.presentation.category_list.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yumyum.domain.model.meals.Category

/**
 * SingleCategoryItem displays a single category item in the categories list.
 * It shows the category image on the left and the category name on the right.
 *
 * This is a reusable component that Compose renders for each item in the LazyColumn.
 *
 * @param categoryItem The category data to display
 * @param onCategoryItemClick Callback function called when user taps this item
 */
@Composable
fun SingleCategoryItem(
    categoryItem: Category,
    onCategoryItemClick: (String) -> Unit
) {
    // Card is a Material Design component that provides elevation and styling
    Card(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
            // clickable() makes the entire card respond to taps
            .clickable { onCategoryItemClick(categoryItem.strCategory) },
        elevation = CardDefaults.cardElevation(
            // Add shadow to the card for visual depth
            defaultElevation = 10.dp
        ),
        colors = CardDefaults.cardColors(
            // Set the card's background color from the app theme
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Row arranges items horizontally (left to right)
        Row(Modifier.fillMaxWidth().padding(PaddingValues(5.dp))) {
            // AsyncImage loads the category image asynchronously from the network
            // This prevents blocking the UI while the image downloads
            AsyncImage(
                model = categoryItem.strCategoryThumb,
                contentDescription = "category-image",
                // FillWidth scales the image to fill the width while maintaining aspect ratio
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .align(CenterVertically)
                    // clip() rounds the corners of the image to match theme shape
                    .clip(MaterialTheme.shapes.medium)
            )
            // Spacer creates empty space between the image and divider
            Spacer(
                modifier = Modifier
                    .width(25.dp)
            )
            // Divider is a thin line that separates the image from the text visually
            Divider(
                // Primary color with transparency for a subtle divider
                color = MaterialTheme.colorScheme.primary.copy(0.4f),
                modifier = Modifier
                    .height(75.dp)
                    .width(1.dp)
                    .align(CenterVertically)
            )
            // Display the category name
            Text(
                text = categoryItem.strCategory,
                // Ellipsis truncates text with "..." if it's too long
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                // Center the text horizontally
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterVertically)
            )
        }
    }
}