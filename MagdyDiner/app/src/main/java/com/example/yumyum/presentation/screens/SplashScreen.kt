package com.example.yumyum.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.yumyum.R
import com.example.yumyum.presentation.navigation.Screen
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

private const val TAG = "YumYum.Splash"

/**
 * AnimationSplashScreen displays an animated splash screen when the app launches.
 * A splash screen is the first screen users see, often with an app logo and animation.
 *
 * This screen uses Lottie, an open-source animation library that plays JSON animations.
 * Lottie allows for smooth, performance-optimized animations created in design tools.
 *
 * @param navController Used to navigate to the next screen after the animation completes
 */
@Composable
fun AnimationSplashScreen(
    navController: NavController
) {
    // Column arranges composables vertically in a stack
    // Modifier.fillMaxSize() makes it take up the entire available screen space
    Column (
        modifier = Modifier.fillMaxSize(),
        // Arrangement.Center centers content vertically
        verticalArrangement = Arrangement.Center,
        // Alignment.CenterHorizontally centers content horizontally
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box {
            // rememberLottieComposition loads the animation from the raw resources folder
            // LottieCompositionSpec.RawRes(R.raw.splash) loads the animation file
            // "by" delegates the value so it's recomputed only when needed
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.splash)
            )

            // animateLottieCompositionAsState tracks the animation's progress (0 to 1)
            // It automatically updates as the animation plays
            val splashAnimationState = animateLottieCompositionAsState(
                composition = composition,
                // play the animation once; when finished we'll navigate away
                iterations = 1
            )

            // Log composition load state
            if (composition == null) {
                Log.d(TAG, "Lottie composition is null")
            } else {
                Log.d(TAG, "Lottie composition loaded")
            }

            // LottieAnimation renders the loaded animation on screen
            LottieAnimation(
                modifier = Modifier.width(240.dp),
                composition = composition,
                // progress tracks how far through the animation we are (0.0 to 1.0)
                progress = { splashAnimationState.progress },
                // applyOpacityToLayers allows fading effects in the animation
                applyOpacityToLayers = true,
                // enableMergePaths enables path merging for better performance
                enableMergePaths = true
            )

            // Use a composable side-effect to navigate once the animation reaches the end
            CheckUserBeforeNavigate(navController, splashAnimationState, composition)
        }
    }

}

/**
 * CheckUserBeforeNavigate waits for the animation to finish, then navigates to the categories screen.
 * This function is private (internal to this file) and only called within this composable.
 *
 * @param navController Used to perform the navigation
 * @param splashAnimationState Contains information about the animation's playback state
 * @param composition The loaded Lottie composition (nullable)
 */
@Composable
private fun CheckUserBeforeNavigate(
    navController: NavController,
    splashAnimationState: LottieAnimationState,
    composition: Any?
) {
    // Primary navigation trigger: when the animation has reached its end
    LaunchedEffect(key1 = splashAnimationState.isAtEnd) {
        if (splashAnimationState.isAtEnd) {
            Log.d(TAG, "Animation reached end — navigating to Categories")
            navController.navigate(Screen.CategoriesScreen.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    // Fallback: if composition is null or animation doesn't finish for any reason,
    // navigate after a short timeout to avoid a stuck/black splash screen.
    LaunchedEffect(key1 = composition) {
        if (composition == null) {
            // wait briefly for composition to load; if still null, navigate
            Log.d(TAG, "Composition null — will navigate after timeout")
            delay(1500)
            Log.d(TAG, "Fallback navigation to Categories")
            navController.navigate(Screen.CategoriesScreen.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }
}