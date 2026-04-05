package edu.northeastern.expensetracker.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository

// @HiltWorker tells Dagger to inject our Repository into this background task
@HiltWorker
class SyncTransactionsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ExpenseRepository
) : CoroutineWorker(context, workerParams) {

    // ... inside doWork() ...
    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "WAKING UP: Checking for offline transactions...") // LOG 1

        return try {
            val unsyncedTransactions = repository.getUnsyncedTransactions()

            if (unsyncedTransactions.isEmpty()) {
                Log.d("SyncWorker", "SLEEPING: No offline transactions found.") // LOG 2
                return Result.success()
            }

            val userHomeCurrency = "INR"

            unsyncedTransactions.forEach { transaction ->
                Log.d("SyncWorker", "FETCHING: Getting live rates for ${transaction.originalCurrency}") // LOG 3

                val rates = repository.getExchangeRates(transaction.originalCurrency)
                val conversionRate = rates[userHomeCurrency]

                if (conversionRate != null) {
                    val updatedTransaction = transaction.copy(
                        exchangeRate = conversionRate,
                        baseAmount = transaction.originalAmount * conversionRate,
                        isSynced = true
                    )

                    repository.updateTransaction(updatedTransaction)
                    Log.d("SyncWorker", "SUCCESS: Updated transaction ${transaction.id} to INR $conversionRate") // LOG 4
                }
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "CRASH: ${e.message}") // LOG 5
            Result.retry()
        }
    }
}