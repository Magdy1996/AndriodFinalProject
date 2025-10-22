package com.example.yumyum.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yumyum.presentation.screens.AnimationSplashScreen
import com.example.yumyum.presentation.screens.CategoriesScreen
import com.example.yumyum.presentation.screens.MealDetailScreen
import com.example.yumyum.presentation.screens.MealsScreen

/**
 * YumYumNavigation sets up the complete navigation graph for the entire application.
 * Navigation determines which screen is displayed and how users move between screens.
 *
 * This uses the Jetpack Navigation library with Compose, which provides:
 * - Back button functionality (automatically managed)
 * - Screen state preservation
 * - Type-safe argument passing between screens
 *
 * The navigation flow in this app is:
 * SplashScreen → CategoriesScreen → MealsScreen → MealDetailScreen
 */
@Composable
fun YumYumNavigation() {
    // rememberNavController creates and remembers a NavController across recompositions
    // NavController is the central API for managing navigation in Compose
    // It keeps track of which screen is currently displayed and the back stack
    val navController = rememberNavController()

    // NavHost is the container for all composable destinations
    // It displays the current screen based on the current navigation route
    NavHost(
        // The NavController manages navigation between composables
        navController = navController,
        // startDestination specifies which screen to show when the app first launches
        startDestination = Screen.SplashScreen.route
    ) {
        // SPLASH SCREEN ROUTE
        // This route displays the animated splash screen
        // When the animation completes, it navigates to the categories screen
        composable(
            route = Screen.SplashScreen.route
        ) {
            AnimationSplashScreen(navController = navController)
        }

        // CATEGORIES SCREEN ROUTE
        // This route displays a list of all meal categories
        // When user taps a category, the onCategoryClick callback navigates to MealsScreen
        composable(
            route = Screen.CategoriesScreen.route
        ) {
            CategoriesScreen(
                onCategoryClick = { strCategory ->
                    // Navigate to MealsScreen and pass the selected category name as a parameter
                    navController.navigate("${Screen.MealsScreen.route}/${strCategory}")
                }
            )
        }

        // MEALS SCREEN ROUTE
        // This route displays meals for a specific category
        // The {strCategory} is a placeholder for the category name parameter
        // Arguments are defined to tell the navigation system what type of data to expect
        composable(
            route = "${Screen.MealsScreen.route}/{strCategory}",
            arguments = listOf(
                // Define that strCategory is a String argument
                navArgument("strCategory") { type = NavType.StringType }
            )
        ) { navBackStackEntry ->
            // Extract the category name from the navigation arguments
            // navBackStackEntry contains the data passed to this route
            navBackStackEntry.arguments?.getString("strCategory")?.let { strCategory ->
                MealsScreen(
                    navController = navController,
                    // When user taps a meal, navigate to MealDetailScreen with meal ID
                    onMealItemClick = { idMeal ->
                        navController.navigate("${Screen.MealDetailScreen.route}/${idMeal}")
                    }
                )
            }
        }

        // MEAL DETAIL SCREEN ROUTE
        // This route displays comprehensive information about a single meal
        // The {idMeal} is a placeholder for the meal ID parameter
        composable(
            route = "${Screen.MealDetailScreen.route}/{idMeal}",
            arguments = listOf(
                // Define that idMeal is a String argument
                navArgument("idMeal") { type = NavType.StringType }
            )
        ) { navBackStackEntry ->
            // Extract the meal ID from the navigation arguments
            navBackStackEntry.arguments?.getString("idMeal")?.let { idMeal ->
                MealDetailScreen(navController = navController)
            }
        }
    }
}
