package com.example.yumyum.genai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A tiny ViewModel that demonstrates calling the GenAI repository and exposing results.
 */
@HiltViewModel
class GenAiViewModel @Inject constructor(private val repo: GenAiRepository): ViewModel() {

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun generate(prompt: String, apiKey: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repo.callOpenAi(prompt, apiKey)
                _result.value = response
            } catch (e: Exception) {
                // Log full stack for debugging
                Log.e("GenAiViewModel", "generate failed", e)
                // Provide explicit, non-null error message to the UI
                val errMsg = "Error (${e.javaClass.simpleName}): ${e.message ?: "no message returned"}"
                _result.value = errMsg
            } finally {
                _loading.value = false
            }
        }
    }
}
