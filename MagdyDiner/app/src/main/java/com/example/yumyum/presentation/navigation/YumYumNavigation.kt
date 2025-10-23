package com.example.yumyum.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.yumyum.presentation.screens.OrdersScreen
import com.example.yumyum.presentation.screens.AnimationSplashScreen
import com.example.yumyum.presentation.screens.CategoriesScreen
import com.example.yumyum.presentation.screens.MealDetailScreen
import com.example.yumyum.presentation.screens.MealsScreen
import com.example.yumyum.presentation.orders.CartScreen
import com.example.yumyum.presentation.auth.LoginScreen

/**
 * YumYumNavigation sets up the complete navigation graph for the entire application.
 * Navigation determines which screen is displayed and how users move between screens.
 *
 * For this app we show the login screen first so users must sign in/up before accessing content.
 */
@Composable
fun YumYumNavigation(navController: NavHostController) {
    // NavHost is the container for all composable destinations
    // It displays the current screen based on the current navigation route
    NavHost(
        navController = navController,
        // Start at the splash screen so the app shows the animated intro first
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

        // ORDERS SCREEN ROUTE
        composable(
            route = Screen.OrdersScreen.route
        ) {
            OrdersScreen(navController = navController)
        }

        // CART SCREEN ROUTE
        composable(
            route = Screen.CartScreen.route
        ) {
            CartScreen(navController = navController)
        }

        // LOGIN / DEBUG SCREEN ROUTE (for switching user id during development)
        composable(
            route = Screen.LoginScreen.route
        ) {
            LoginScreen(navController = navController)
        }
    }
}
