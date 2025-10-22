package com.example.yumyum.presentation.meal_detail.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yumyum.domain.model.meals.MealDetail

/**
 * MealDetailItem displays the basic information about a meal.
 * It shows the meal image, name, category, and cuisine area/region.
 * This component is the first section of the meal detail screen.
 *
 * @param mealInfo The detailed meal information to display
 */
@Composable
fun MealDetailItem(
    mealInfo: MealDetail
) {
    // Column arranges items vertically in a centered layout
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Container for the meal thumbnail with fixed dimensions
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(MaterialTheme.shapes.medium)
        ) {
            // Load and display the meal thumbnail image
            AsyncImage(
                model = mealInfo.strMealThumb,
                contentDescription = "dish-image",
                // Fit scales the image to fit within the container
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
            )
        }
        // Display the meal name
        Text(
            text = mealInfo.strMeal,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        )
        // Display the meal's category (e.g., "Dessert", "Seafood", etc.)
        Text(
            text = mealInfo.strCategory,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
        // Display the meal's cuisine area/region (e.g., "Italian", "Chinese", etc.)
        Text(
            text = mealInfo.strArea,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        )
        // Visual divider separating meal info from the ingredients section
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 10.dp)
        )
    }
}

/**
 * MealIngredients displays a list of all ingredients needed for the meal.
 * Each ingredient is shown with its measurement (e.g., "2 cups", "1 tablespoon").
 *
 * @param mealInfo The meal information containing ingredient and measurement data
 */
@Composable
fun MealIngredients(
    mealInfo: MealDetail
) {
    // Create a list of ingredient-measurement pairs from the meal data
    // The API returns up to 10 ingredients with their measurements
    val ingredients = listOf(
        mealInfo.strIngredient1 to mealInfo.strMeasure1,
        mealInfo.strIngredient2 to mealInfo.strMeasure2,
        mealInfo.strIngredient3 to mealInfo.strMeasure3,
        mealInfo.strIngredient4 to mealInfo.strMeasure4,
        mealInfo.strIngredient5 to mealInfo.strMeasure5,
        mealInfo.strIngredient6 to mealInfo.strMeasure6,
        mealInfo.strIngredient7 to mealInfo.strMeasure7,
        mealInfo.strIngredient8 to mealInfo.strMeasure8,
        mealInfo.strIngredient9 to mealInfo.strMeasure9,
        mealInfo.strIngredient10 to mealInfo.strMeasure10
    )

    // Display ingredients in a vertically spaced column layout
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        // Iterate through each ingredient-measurement pair
        ingredients.forEach { (ingredient, measure) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bullet point indicator for each ingredient
                Box(
                    modifier = Modifier
                        .padding(start = 15.dp, end = 10.dp)
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
                // Display ingredient name and measurement (e.g., "Chicken - 500g")
                Text(
                    text = "$ingredient - $measure",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * MealInstructions displays the cooking instructions for the meal.
 * The instructions are formatted with proper line breaks and justified alignment.
 *
 * @param mealInfo The meal information containing the cooking instructions
 */
@Composable
fun MealInstructions(
    mealInfo: MealDetail
) {
    // Format the cooking instructions by normalizing line breaks
    // This replaces escape sequences and adds extra spacing between paragraphs
    val instructions = mealInfo.strInstructions
        // Replace escaped newline characters
        .replace("\\r\\n", "\n")
        // Add extra space between paragraphs for better readability
        .replace("\n", "\n\n")
        // Remove leading/trailing whitespace
        .trim()

    // Display the formatted instructions in a justified layout
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        // Align content to the top-start (top-left)
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = instructions,
            // Justify aligns text to both left and right edges
            textAlign = TextAlign.Justify,
            color = MaterialTheme.colorScheme.onSurface,
            // Set line height for better readability
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
    }
}