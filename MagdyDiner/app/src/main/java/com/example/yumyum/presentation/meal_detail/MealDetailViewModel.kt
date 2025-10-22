package com.example.yumyum.presentation.meal_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyum.common.Constants.PARAM_ID_MEAL
import com.example.yumyum.common.Resource
import com.example.yumyum.domain.use_case.ApiUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MealDetailViewModel manages the data and state for the meal detail screen.
 *
 * It receives the meal ID from navigation and fetches comprehensive information
 * about that specific meal, including ingredients and cooking instructions.
 *
 * @param apiUseCases Injected use cases for API operations
 * @param savedStateHandle Contains the meal ID from navigation arguments
 */
@HiltViewModel
class MealDetailViewModel @Inject constructor(
    private val apiUseCases: ApiUseCases,
    // SavedStateHandle provides access to the meal ID passed from navigation
    savedStateHandle: SavedStateHandle
): ViewModel() {
    // Mutable state that holds the meal detail, loading, and error states
    private val _state = MutableStateFlow(MealDetailState())
    // Expose the state as an immutable StateFlow for UI observation
    val state: StateFlow<MealDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Extract the meal ID from navigation arguments
            // The meal ID is passed when navigating from the meals list
            savedStateHandle.get<String>(PARAM_ID_MEAL)?.let { idMeal ->
                // If meal ID exists, fetch detailed information about this meal
                getMeal(idMeal)
            }
        }
    }

    /**
     * Fetches detailed information about a specific meal.
     *
     * @param idMeal The unique identifier of the meal to fetch
     */
    private suspend fun getMeal(idMeal: String) {
        // Call the use case to get meal details
        apiUseCases.getMealUseCase(idMeal).onEach { result ->
            when(result) {
                // Success - we received meal details from the API
                is Resource.Success -> {
                    _state.value = MealDetailState(
                        // Convert to list or use empty list if null
                        meals = result.data?.meals ?: emptyList()
                    )
                }
                // Error - meal details could not be fetched
                is Resource.Error -> {
                    _state.value = MealDetailState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                // Loading - meal details are being fetched
                is Resource.Loading -> {
                    _state.value = MealDetailState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}