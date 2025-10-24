@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.yumyum.presentation.genai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yumyum.genai.GenAiViewModel
import com.example.yumyum.BuildConfig

/**
 * Small screen to demo the Generative AI integration.
 * Only the prompt is shown; the OpenAI API key is read from BuildConfig and never displayed.
 */
@Composable
fun GenAiDemoScreen(viewModel: GenAiViewModel = hiltViewModel()) {
    val prompt = remember { mutableStateOf("Write a friendly welcome message for a food-ordering app homepage.") }

    // Read API key from BuildConfig (populated from local.properties). Do NOT display the key.
    val apiKey = BuildConfig.OPENAI_API_KEY
    val apiConfigured = apiKey.isNotBlank()

    val loading by viewModel.loading.collectAsState(initial = false)
    val result by viewModel.result.collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Informational status — do not reveal the key value
        if (apiConfigured) {
            Text(text = "OpenAI API key is configured (hidden for security).")
        } else {
            Text(text = "No OpenAI API key configured. Add OPENAI_API_KEY to local.properties and sync the project.")
        }

        OutlinedTextField(
            value = prompt.value,
            onValueChange = { prompt.value = it },
            label = { Text("Prompt") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        )

        Button(
            onClick = {
                if (apiConfigured) {
                    viewModel.generate(prompt.value, apiKey)
                } else {
                    // If not configured, still call with empty key so the ViewModel can show error message
                    viewModel.generate(prompt.value, "")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate")
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }

        result?.let {
            Text(text = "Result:\n$it", modifier = Modifier.padding(top = 12.dp))
        }
    }
}
