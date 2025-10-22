package com.example.yumyum.presentation.category_list

import com.example.yumyum.domain.model.meals.Category

/**
 * CategoryListState is a data class that represents the UI state for the categories screen.
 *
 * A data class is used because:
 * - It automatically generates equals(), hashCode(), and toString() methods
 * - It can be copied with copy() to create modified versions
 * - It's ideal for holding immutable state data
 *
 * This state pattern allows the UI to automatically update whenever the state changes.
 * The ViewModel maintains this state and updates it based on API responses.
 */
data class CategoryListState(
    // Whether data is currently being fetched from the API
    // The UI shows a loading spinner when this is true
    val isLoading: Boolean = false,
    // List of all available meal categories fetched from the API
    // Empty list is the default value before data is loaded
    val categories: List<Category> = emptyList(),
    // Error message if something goes wrong during data fetching
    // Empty string means no error occurred
    val error: String = ""
)


