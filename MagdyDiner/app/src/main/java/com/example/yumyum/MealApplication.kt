package com.example.yumyum

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * MealApplication extends the Android Application class to serve as the global application state holder.
 *
 * The @HiltAndroidApp annotation initializes Hilt dependency injection at the application level.
 * This must be done in an Application class to enable Hilt to inject dependencies throughout the app.
 *
 * When the app starts, this class is instantiated first before any activities or services,
 * making it an ideal place to initialize global resources and dependency injection.
 */
@HiltAndroidApp
class MealApplication: Application()