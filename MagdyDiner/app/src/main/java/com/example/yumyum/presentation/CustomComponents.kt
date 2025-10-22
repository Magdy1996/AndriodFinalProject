package com.example.yumyum.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.yumyum.R

/**
 * HeadingTextComponent is a reusable composable for displaying large heading text.
 *
 * Reusable components reduce code duplication and make the app consistent.
 * Instead of writing the same Text() code everywhere, we create it once here.
 *
 * @param text The text content to display
 * @param textAlign How to align the text (Start, Center, End)
 * @param modifier Optional layout and styling parameters
 */
@Composable
fun HeadingTextComponent(
    text: String,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    // Display text using the Material Design typography system
    // This ensures consistency with the app's design language
    Text(
        text = text,
        // Use the app's primary text color from the theme
        color = MaterialTheme.colorScheme.onBackground,
        // Apply the largest typography style for emphasis
        style = MaterialTheme.typography.headlineLarge,
        textAlign = textAlign,
        modifier = modifier
    )

}

/**
 * TextTitleMealInfo is a reusable composable for section titles in meal information.
 * Used for headers like "Ingredients" and "Instructions" on the meal detail screen.
 *
 * @param text The section title text to display
 */
@Composable
fun TextTitleMealInfo(text: String) {
    // Display a medium-sized title in the app's primary color
    Text(
        text = text,
        // Use the primary color to make section titles stand out
        color = MaterialTheme.colorScheme.primary,
        // Apply the title typography style
        style = MaterialTheme.typography.titleLarge,
        // Align text to the left side
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )

}

/**
 * ProgressBar displays a centered loading indicator.
 * Shown while the app is fetching data from the API.
 *
 * This makes it clear to the user that something is loading.
 */
@Composable
fun ProgressBar() {
    // Ensure the whole screen uses the theme background so the app window is not just black
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Use primary color so the indicator is visible on dark/light themes
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * ImageLogoComponent displays the app's logo with theme color tinting.
 * The logo is typically shown on the splash screen or login screen.
 *
 * Tinting allows us to change the logo color to match the app theme.
 */
@Composable
fun ImageLogoComponent() {
    val imageColor = androidx.compose.ui.graphics.ColorFilter.tint(
        MaterialTheme.colorScheme.primary
    )

    // Wrap logo in a Surface to ensure the background matches the app theme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .size(150.dp),
                painter = painterResource(R.drawable.logo),
                contentDescription = "app-logo",
                colorFilter = imageColor
            )
        }
    }
}

/**
 * TextTitleComponent displays a centered title text.
 * Used for displaying screen titles or important text across the full width.
 *
 * @param text The title text to display
 */
@Composable
fun TextTitleComponent(text: String) {
    // Display text that spans the full width and is centered
    Text(
        text = text,
        // Use the primary text color
        color = MaterialTheme.colorScheme.onBackground,
        // Apply the title typography style
        style = MaterialTheme.typography.titleLarge,
        // Center the text horizontally
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
    )
}

// Debug helper: show a visible placeholder screen so you can quickly point MainActivity.setContent { DebugEmptyScreenPlaceholder() }
@Composable
fun DebugEmptyScreenPlaceholder(message: String = "DEBUG: App content is empty. Update MainActivity.setContent") {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = message, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
