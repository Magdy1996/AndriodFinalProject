package com.example.yumyum.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Helper to schedule SyncWorker tasks.
 */
object WorkScheduler {

    private const val UNIQUE_PERIODIC_WORK_NAME = "sync_worker_periodic"
    private const val UNIQUE_PERIODIC_GENAI_WORK_NAME = "genai_worker_periodic"

    fun schedulePeriodicSync(context: Context, repeatIntervalHours: Long = 6) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(repeatIntervalHours, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    fun enqueueOneOffSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        WorkManager.getInstance(context)
            .enqueue(request)
    }

    // New: schedule periodic GenAI worker to refresh generated messages
    fun schedulePeriodicGenAi(context: Context, repeatIntervalHours: Long = 12) {
        val request = PeriodicWorkRequestBuilder<GenAiWorker>(repeatIntervalHours, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_GENAI_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    // New: enqueue a one-off GenAI generation (for manual refresh)
    fun enqueueOneOffGenAi(context: Context) {
        val request = OneTimeWorkRequestBuilder<GenAiWorker>()
            .build()

        WorkManager.getInstance(context)
            .enqueue(request)
    }
}
