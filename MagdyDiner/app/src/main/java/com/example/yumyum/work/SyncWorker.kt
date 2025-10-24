package com.example.yumyum.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import kotlinx.coroutines.delay
import android.util.Log

/**
 * A simple CoroutineWorker that simulates syncing data in the background.
 * It can be scheduled periodically or as a one-off work request.
 */
class SyncWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Log.d("SyncWorker", "Starting background sync")
        // Simulate some work (e.g., sync with remote, push local orders)
        try {
            delay(1500)
            // Here you would call repository.syncOrders() or similar
            Log.d("SyncWorker", "Sync completed successfully")
            return ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            return ListenableWorker.Result.retry()
        }
    }
}
