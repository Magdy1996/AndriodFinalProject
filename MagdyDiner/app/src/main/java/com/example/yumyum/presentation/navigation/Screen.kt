package com.example.yumyum.presentation.navigation

/**
 * Screen is a sealed class that defines all the different screens in the application.
 * A sealed class restricts inheritance to only the classes defined within this file,
 * making it type-safe to use in when statements.
 *
 * Each screen is represented as an object (singleton) that contains a route string.
 * The route string is used by the navigation system to identify which screen to display.
 *
 * Benefits of using sealed classes for navigation:
 * - Type-safe: The compiler ensures all screens are handled in navigation
 * - Centralized: All screen definitions are in one place
 * - Easy to modify: Adding a new screen only requires adding one line here
 */
sealed class Screen(val route: String) {
    // The splash screen shown when the app first launches (with animation)
    object SplashScreen: Screen("splash_screen")
    // The categories screen displays a list of meal categories
    object CategoriesScreen: Screen("categories_screen")
    // The meals screen displays meals for a selected category
    object MealsScreen: Screen("meals_screen")
    // The meal detail screen shows complete information about a specific meal
    object MealDetailScreen: Screen("meal_detail_screen")
}
