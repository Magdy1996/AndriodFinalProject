package com.example.yumyum.presentation.meals_list

import com.example.yumyum.domain.model.meals.Meals

/**
 * MealsListState represents the UI state for the meals list screen.
 *
 * Similar to CategoryListState, this data class holds:
 * - Loading state to show/hide progress indicator
 * - List of meals fetched from the API
 * - Error message if data fetching fails
 *
 * The UI observes this state and updates automatically when any property changes.
 */
data class MealsListState(
    // Whether meals are currently being fetched from the API
    val isLoading: Boolean = false,
    // List of meals for the selected category
    val meals: List<Meals> = emptyList(),
    // Error message if something goes wrong
    val error: String = ""
)
