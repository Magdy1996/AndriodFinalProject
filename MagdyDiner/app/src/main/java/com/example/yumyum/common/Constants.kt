package com.example.yumyum.common

/**
 * Constants is an object that holds all constant values used throughout the application.
 * Using an object instead of a class means only one instance will ever exist (singleton pattern).
 * This is the recommended approach in Kotlin for storing global constants.
 *
 * Benefits of centralizing constants:
 * - Easy to find and update values from a single location
 * - Prevents typos by using const properties instead of hardcoded strings
 * - Makes the code more maintainable and less error-prone
 */
object Constants {
    // API Configuration - These values define how the app communicates with external services
    // SERVER_CLIENT is used for Google authentication/login functionality
    const val SERVER_CLIENT = "678510979604-4es9kacnjkf6as15u070kugthf3rfl43.apps.googleusercontent.com"
    // BASE_URL is the root endpoint for all API requests to the meal database
    const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    // Navigation Parameter Keys - These are used to pass data between screens
    // When navigating from categories to meals, we pass the category name using PARAM_STR_CATEGORY
    const val PARAM_STR_CATEGORY = "strCategory"
    // When navigating from meals to meal details, we pass the meal ID using PARAM_ID_MEAL
    const val PARAM_ID_MEAL = "idMeal"

    // Firestore Collection Names - These define the structure of data in the database
    // All user documents are stored in a "users" collection in Firebase Firestore
    const val USERS = "users"

    // User Document Fields - These are the individual properties stored for each user
    // Each user document contains these fields to store their profile information
    const val DISPLAY_NAME = "displayName"    // The name the user chooses to display
    const val EMAIL = "email"                 // The user's email address
    const val PHOTO_URL = "photoUrl"         // URL pointing to the user's profile picture
    const val CREATED_AT = "createdAt"       // Timestamp of when the user account was created

    // Authentication Request Types - These distinguish different authentication flows
    // SIGN_IN_REQUEST is used when a user is logging into an existing account
    const val SIGN_IN_REQUEST = "signInRequest"
    // SIGN_UP_REQUEST is used when a user is creating a new account
    const val SIGN_UP_REQUEST = "signUpRequest"

    // UI Button Labels - These are the text strings displayed on buttons in the user interface
    const val SIGN_IN_WITH_GOOGLE = "Sign in with Google"  // Button text for Google login
    const val SIGN_OUT = "Sign-out"                          // Button text for logging out
    const val DELETE_ACCOUNT = "Delete account"             // Button text for account deletion

    // User Messages - These are informational or error messages shown to the user
    // This message informs the user they need to re-authenticate before deleting their account
    const val DELETE_ACCOUNT_MESSAGE = "You need to re-authenticate before delete account."

}