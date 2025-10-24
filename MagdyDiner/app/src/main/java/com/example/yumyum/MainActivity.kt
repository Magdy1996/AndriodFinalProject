package com.example.yumyum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.yumyum.presentation.auth.LoginViewModel
import com.example.yumyum.presentation.navigation.Screen
import com.example.yumyum.presentation.navigation.YumYumNavigation
import com.example.yumyum.ui.theme.YumYumTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

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
                    // Use a Scaffold with a persistent top app bar containing a debug Switch User icon
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YumYumApp() {
    val navController: NavHostController = rememberNavController()

    // Obtain the LoginViewModel here so the top app bar can show a logout button
    val loginViewModel: LoginViewModel = hiltViewModel()
    val currentUserId by loginViewModel.currentUserId.collectAsState(initial = 0L)
    // Controls whether the logout confirmation dialog is shown
    var showLogoutDialog by remember { mutableStateOf(false) }
    // Tracks whether sign-out is currently in progress so UI can show a loading indicator
    var isSigningOut by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    Row {
                        // Switch User / Debug button
                        IconButton(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                            Icon(Icons.Default.Person, contentDescription = "Switch User")
                        }

                        // Preferences debug (settings) button — opens the DataStore debug screen
                        IconButton(onClick = { navController.navigate(Screen.PreferencesDebugScreen.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Preferences Debug")
                        }

                        // Always display the Logout icon so it's discoverable.
                        // If a user is signed in, tapping shows a confirmation dialog.
                        // If no user is signed in, tapping navigates to the login screen.
                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (currentUserId != 0L) {
                                showLogoutDialog = true
                            } else {
                                Toast.makeText(context, "No user signed in", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.LoginScreen.route) {
                                    popUpTo(navController.graph.id) { inclusive = false }
                                }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }
                 },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            YumYumNavigation(navController)

            // Logout confirmation dialog (overlay)
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isSigningOut) showLogoutDialog = false },
                    title = { Text("Confirm logout") },
                    text = { Text("Are you sure you want to log out of your account?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // start the sign-out flow and show loading UI; navigation is handled when currentUserId updates
                                isSigningOut = true
                                loginViewModel.signOut()
                            },
                            enabled = !isSigningOut
                        ) {
                            if (isSigningOut) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Logout")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { if (!isSigningOut) showLogoutDialog = false }, enabled = !isSigningOut) { Text("Cancel") }
                    }
                )
            }

            // When sign-out completes (currentUserId becomes 0) and we were signing out, navigate and close dialog
            androidx.compose.runtime.LaunchedEffect(currentUserId) {
                if (isSigningOut && currentUserId == 0L) {
                    // navigation after sign-out
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                    isSigningOut = false
                    showLogoutDialog = false
                }
            }
         }
     }
 }
