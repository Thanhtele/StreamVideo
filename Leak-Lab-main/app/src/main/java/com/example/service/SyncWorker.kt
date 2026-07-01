package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        // Static diagnostics cache to monitor and keep records of worker tasks
        val activeWorkers = mutableListOf<SyncWorker>()
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Register worker in telemetry list
        activeWorkers.add(this@SyncWorker)

        val syncRepository = ServiceLocator.syncLogRepository ?: return@withContext Result.failure()
        
        try {
            val startTime = System.currentTimeMillis()
            
            // Simulating network sync operations
            kotlinx.coroutines.delay(2000)
            
            val duration = System.currentTimeMillis() - startTime
            syncRepository.addSyncLog(
                actionName = "WorkManager Sync",
                status = "SUCCESS",
                durationMs = duration,
                details = "Successfully synchronized user data and message cache."
            )
            
            Result.success()
        } catch (e: Exception) {
            syncRepository.addSyncLog(
                actionName = "WorkManager Sync",
                status = "FAILED",
                durationMs = 0L,
                details = e.message ?: "Unknown sync error"
            )
            Result.failure()
        } finally {
            // Completed transaction processing
        }
    }
}
