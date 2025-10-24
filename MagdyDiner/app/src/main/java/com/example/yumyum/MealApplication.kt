package com.example.yumyum

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.yumyum.work.WorkScheduler
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import javax.inject.Inject

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
class MealApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Schedule periodic background sync using WorkManager
        WorkScheduler.schedulePeriodicSync(this, repeatIntervalHours = 6)
        // Schedule periodic GenAI generation (writes friendly message to DataStore)
        WorkScheduler.schedulePeriodicGenAi(this, repeatIntervalHours = 24)
    }

    // Provide WorkManager configuration so Hilt can inject Worker dependencies
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
