package com.example.yumyum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.yumyum.presentation.navigation.YumYumNavigation
import com.example.yumyum.ui.theme.YumYumTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity is the entry point of the Android application.
 * It extends ComponentActivity to use Jetpack Compose for UI instead of traditional XML layouts.
 * The @AndroidEntryPoint annotation enables Hilt dependency injection in this Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * onCreate is called when the activity is first created.
     * This is where we set up the UI and initialize the application.
     *
     * @param savedInstanceState Contains data from a previous instance if the activity was destroyed and recreated
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent is used to set the composable content for this activity
        // This replaces the traditional setContentView(R.layout.activity_main) approach
        setContent {
            // Apply the app's theme styling to all child composables
            YumYumTheme {
                // Surface provides a background container for the content
                // It applies the theme's background color from the material design system
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call the main app composable to display the application UI
                    YumYumApp()
                }
            }
        }
    }
}

/**
 * YumYumApp is the root composable function that sets up the application's navigation.
 * This function is called from MainActivity and contains the app's main navigation graph.
 */
@Composable
fun YumYumApp() {
    // Initialize the navigation system which manages screen transitions
    YumYumNavigation()
}
