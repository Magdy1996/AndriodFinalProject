package com.example.yumyum.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result as WorkResult
import com.example.yumyum.genai.GenAiRepository
import com.example.yumyum.data.preferences.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Hilt-enabled CoroutineWorker that calls GenAiRepository to generate a message
 * and persists it to UserPreferences. This demonstrates a meaningful GenAI usage
 * in a background task.
 */
@HiltWorker
class GenAiWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: GenAiRepository,
    private val prefs: UserPreferences
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): WorkResult {
        Log.d("GenAiWorker", "Starting GenAI background generation")
        try {
            // Example prompt for periodic homepage welcome text
            val prompt = "Write a friendly, concise welcome message for the MagdyDiner food-ordering app homepage. Keep it under 30 words."

            // Read API key from BuildConfig — repository already validates the key
            val apiKey = com.example.yumyum.BuildConfig.OPENAI_API_KEY

            val generated = try {
                repo.callOpenAi(prompt, apiKey)
            } catch (e: Exception) {
                Log.e("GenAiWorker", "GenAI call failed", e)
                null
            }

            if (!generated.isNullOrBlank()) {
                // Save generated message to DataStore for the UI to display later
                prefs.setGeneratedMessage(generated)
                Log.d("GenAiWorker", "Saved generated message to DataStore")
                return WorkResult.success()
            } else {
                Log.w("GenAiWorker", "No generated output to save")
                return WorkResult.retry()
            }
        } catch (e: Exception) {
            Log.e("GenAiWorker", "Worker failed", e)
            return WorkResult.retry()
        }
    }
}
