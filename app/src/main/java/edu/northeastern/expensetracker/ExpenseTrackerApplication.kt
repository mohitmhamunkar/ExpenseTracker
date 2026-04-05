package edu.northeastern.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import edu.northeastern.expensetracker.data.worker.SyncTransactionsWorker
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApplication : Application(), Configuration.Provider {

    // 1. Inject the Hilt factory
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // 2. Tell Android to use this factory whenever it builds a background worker
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // --- ADD THIS ENTIRE ONCREATE BLOCK ---
    override fun onCreate() {
        super.onCreate()

        // 1. The Rule: Only run this worker when the phone has an active internet connection
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 2. The Job: Package our SyncWorker with the network rule
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncTransactionsWorker>()
            .setConstraints(constraints)
            .build()

        // 3. The Trigger: Hand it to the Android OS to manage in the background
        WorkManager.getInstance(this).enqueueUniqueWork(
            "OfflineTransactionSync",
            ExistingWorkPolicy.KEEP, // If it's already running, don't restart it
            syncWorkRequest
        )
    }
}