package edu.northeastern.expensetracker.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import edu.northeastern.expensetracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncTransactionsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ExpenseRepository,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Bypass the DAO query and filter locally
            val allTransactions = repository.getAllTransactions().first()
            val unsyncedTransactions = allTransactions.filter { !it.isSynced }

            if (unsyncedTransactions.isEmpty()) {
                return Result.success()
            }

            val userHomeCurrency = preferencesRepository.homeCurrency.first()
            val globalRates = repository.getExchangeRates("USD")

            unsyncedTransactions.forEach { transaction ->
                try {
                    val rateForOriginal = globalRates[transaction.originalCurrency]
                    val rateForHome = globalRates[userHomeCurrency]

                    if (rateForOriginal != null && rateForHome != null) {
                        val conversionRate = rateForHome / rateForOriginal

                        val updatedTransaction = transaction.copy(
                            exchangeRate = conversionRate,
                            baseAmount = transaction.originalAmount * conversionRate,
                            isSynced = true
                        )

                        repository.updateTransaction(updatedTransaction)
                    } else {
                        Log.e("SyncWorker", "Missing rates for ${transaction.originalCurrency} or $userHomeCurrency")
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Math failed for ${transaction.originalCurrency}: ${e.message}")
                }
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Fatal worker crash: ${e.message}")
            Result.retry()
        }
    }
}