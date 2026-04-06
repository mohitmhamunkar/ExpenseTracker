package edu.northeastern.expensetracker.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.northeastern.expensetracker.data.worker.SyncTransactionsWorker
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import edu.northeastern.expensetracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val repository: ExpenseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val currentCurrency = preferencesRepository.homeCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "INR"
    )

    val availableCurrencies = listOf("INR", "USD", "EUR", "GBP", "JPY", "CAD")

    fun setHomeCurrency(newCurrency: String) {
        viewModelScope.launch {
            val oldCurrency = preferencesRepository.homeCurrency.first()

            if (oldCurrency != newCurrency) {
                preferencesRepository.saveHomeCurrency(newCurrency)

                withContext(Dispatchers.IO + NonCancellable) {
                    val allTransactions = repository.getAllTransactions().first()

                    allTransactions.forEach { transaction ->
                        repository.updateTransaction(
                            transaction.copy(isSynced = false)
                        )
                    }

                    // Give the physical disk time to finish writing
                    delay(1500)

                    val syncRequest = OneTimeWorkRequestBuilder<SyncTransactionsWorker>().build()
                    WorkManager.getInstance(context).enqueue(syncRequest)
                }
            }
        }
    }
}