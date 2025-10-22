package com.example.yumyum.presentation.meals_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyum.common.Constants.PARAM_STR_CATEGORY
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
 * MealsListViewModel manages the data and state for the meals list screen.
 *
 * It receives the category parameter from navigation and fetches all meals in that category.
 * The SavedStateHandle allows the ViewModel to access navigation arguments.
 *
 * @param apiUseCases Injected use cases for API operations
 * @param savedStateHandle Contains navigation arguments passed to this screen
 */
@HiltViewModel
class MealsListViewModel @Inject constructor(
    private val apiUseCases: ApiUseCases,
    // SavedStateHandle provides access to navigation arguments
    // This survives configuration changes, ensuring we don't lose the category name
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Mutable state that holds the meals list, loading, and error states
    private val _state = MutableStateFlow(MealsListState())
    // Expose the state as an immutable StateFlow for UI observation
    val state: StateFlow<MealsListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Extract the category name from navigation arguments
            // savedStateHandle.get() retrieves the value passed from the navigation route
            savedStateHandle.get<String>(PARAM_STR_CATEGORY)?.let { strCategory ->
                // If category exists, fetch meals for that category
                getMeals(strCategory)
            }
        }
    }

    /**
     * Fetches meals for a specific category and updates the state.
     *
     * @param strCategory The category name to fetch meals for
     */
    private suspend fun getMeals(strCategory: String) {
        // Call the use case to get meals for this category
        apiUseCases.getMealsUseCase(strCategory).onEach { result ->
            when (result) {
                // Success - we got meals from the API
                is Resource.Success -> {
                    _state.value = MealsListState(
                        // Use the null-coalescing operator ?: to default to empty list
                        meals = result.data?.meals ?: emptyList()
                    )
                }
                // Error - something went wrong fetching meals
                is Resource.Error -> {
                    _state.value = MealsListState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                // Loading - meals are being fetched
                is Resource.Loading -> {
                    _state.value = MealsListState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}