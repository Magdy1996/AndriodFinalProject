package com.example.yumyum.presentation.category_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * CategoryListViewModel manages the data and state for the categories screen.
 *
 * A ViewModel is a component that:
 * - Survives configuration changes (like screen rotation)
 * - Holds and manages UI state
 * - Communicates with data sources (repositories)
 * - Notifies the UI when data changes
 *
 * The @HiltViewModel annotation enables Hilt to automatically create and inject this ViewModel.
 */
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    // The apiUseCases is injected by Hilt from the AppModule
    // It contains all the use cases needed for API operations
    private val apiUseCases: ApiUseCases
): ViewModel() {

    // Mutable state that can only be modified within this ViewModel
    // _state starts with private visibility to prevent external modification
    private val _state = MutableStateFlow(CategoryListState())

    // Expose the state as a read-only StateFlow to the UI
    // StateFlow is a Flow that always has a current value and emits updates
    // UI components use collectAsState() to observe changes
    val state: StateFlow<CategoryListState> = _state.asStateFlow()

    // init block runs automatically when the ViewModel is created
    // This is where we kick off data loading
    init {
        viewModelScope.launch {
            // viewModelScope ensures coroutines are cancelled when the ViewModel is destroyed
            // This prevents memory leaks from tasks running after the ViewModel is gone
            getCategories()
        }
    }

    /**
     * Fetches categories from the API and updates the state based on the result.
     * This is a suspended function that runs within a coroutine.
     */
    private suspend fun getCategories() {
        // apiUseCases.getCategoriesUseCase() returns a Flow that emits results
        // onEach allows us to react to each emission
        apiUseCases.getCategoriesUseCase().onEach { result ->
            when (result) {
                // Success case - we got data from the API
                is Resource.Success -> {
                    // Update the state with the fetched categories
                    // The !! operator assumes result.data is not null
                    _state.value = CategoryListState(
                        categories = result.data!!.categories
                    )
                }
                // Error case - something went wrong
                is Resource.Error -> {
                    // Update the state with an error message
                    // The ?: operator provides a default message if result.message is null
                    _state.value = CategoryListState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                // Loading case - data is being fetched
                is Resource.Loading -> {
                    // Update the state to indicate loading is in progress
                    _state.value = CategoryListState(isLoading = true)
                }
            }
        // launchIn(viewModelScope) starts collecting the flow in the ViewModel's scope
        // This ensures it stops when the ViewModel is destroyed
        }.launchIn(viewModelScope)
    }

}