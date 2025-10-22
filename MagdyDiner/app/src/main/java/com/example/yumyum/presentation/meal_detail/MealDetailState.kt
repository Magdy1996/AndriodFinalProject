package com.example.yumyum.presentation.meal_detail

import com.example.yumyum.domain.model.meals.MealDetail

/**
 * MealDetailState represents the UI state for the meal detail screen.
 *
 * This state holds:
 * - Loading flag to show/hide the progress indicator
 * - List of meal details (typically contains just one meal)
 * - Error message if data fetching fails
 *
 * The screen displays the first meal in the list using firstOrNull()
 */
data class MealDetailState(
    // Whether meal details are currently being fetched
    val isLoading: Boolean = false,
    // List containing the detailed information about a single meal
    val meals: List<MealDetail> = emptyList(),
    // Error message if something went wrong during fetching
    val error: String = ""
)