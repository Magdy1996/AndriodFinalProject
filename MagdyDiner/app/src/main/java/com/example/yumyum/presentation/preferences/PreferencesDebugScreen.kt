package com.example.yumyum.presentation.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Simple debug screen to view and toggle DataStore-backed preferences.
 * Use this in development to verify persistence without digging into device files.
 */
@Composable
fun PreferencesDebugScreen(viewModel: PreferencesViewModel = hiltViewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val theme by viewModel.currentTheme.collectAsState()
    val userId by viewModel.currentUserId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Preferences Debug")
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "isLoggedIn: $isLoggedIn")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "currentUserId: ${userId ?: "(none)"}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "currentTheme: $theme")

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // Toggle login state: when logging in set a sample id; when logging out clear id
                if (isLoggedIn) {
                    viewModel.setLoggedIn(false, null)
                } else {
                    // use a deterministic test id so it's easy to check persisted value
                    viewModel.setLoggedIn(true, "1001")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoggedIn) "Sign out (persist)" else "Sign in as test user (persist)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val next = if (theme == "light") "dark" else "light"
                viewModel.setTheme(next)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Toggle theme (light/dark)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Clear stored user id but keep logged-in flag true for testing
                viewModel.setLoggedIn(true, null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Set loggedIn=true, clear userId")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Reset to defaults
                viewModel.setTheme("light")
                viewModel.setLoggedIn(false, null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Reset preferences")
        }
    }
}

