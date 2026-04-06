package edu.northeastern.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.northeastern.expensetracker.domain.model.Transaction
import edu.northeastern.expensetracker.domain.repository.UserPreferencesRepository
import edu.northeastern.expensetracker.domain.use_case.ExpenseUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Currency
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val useCases: ExpenseUseCases,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // This is the private pipeline where we update the data
    private val _state = MutableStateFlow(ExpenseState())
    // This is the public pipeline that the UI watches
    val state: StateFlow<ExpenseState> = _state.asStateFlow()

    // The user's anchor currency (can be updated from a Settings screen later)
    val userHomeCurrency = preferencesRepository.homeCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "INR"
    )

    // THE UPGRADE: Automatically translates "USD" to "$", "INR" to "₹", etc.
    val currencySymbol: StateFlow<String> = userHomeCurrency.map { code ->
        try {
            Currency.getInstance(code).symbol
        } catch (e: Exception) {
            code // Fallback to the text code if the symbol isn't found
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "₹"
    )

    private val eventChannel = Channel<String>()
    val uiEvent = eventChannel.receiveAsFlow()

    init {
        // The second the ViewModel is born, we tell it to fetch the transactions
        getTransactions()
    }

    private fun getTransactions() {
        useCases.getTransactions().onEach { newTransactions ->
            _state.value = state.value.copy(
                transactions = newTransactions
            )
        }.launchIn(viewModelScope)
    }

    fun addTransaction(
        amount: Double,
        selectedCurrency: String,
        categoryId: String,
        notes: String,
        date: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            var finalRate = 1.0
            var finalBaseAmount = amount
            var finalMessage = "SUCCESS"

            var wasSuccessfullySynced = true

            if (selectedCurrency != userHomeCurrency.value) {
                val rates = useCases.getExchangeRates(selectedCurrency)
                val conversionRate = rates[userHomeCurrency.value]

                if (conversionRate != null) {
                    finalRate = conversionRate
                    finalBaseAmount = amount * conversionRate
                } else {
                    finalMessage = "Offline: Saved using 1:1 rate for $selectedCurrency"
                    wasSuccessfullySynced = false
                }
            }

            val newTransaction = edu.northeastern.expensetracker.domain.model.Transaction(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                originalAmount = amount,
                originalCurrency = selectedCurrency,
                exchangeRate = finalRate,
                baseAmount = finalBaseAmount,
                date = date,
                notes = notes.trim(),
                isSynced = wasSuccessfullySynced
            )

            useCases.addTransaction(newTransaction)
            eventChannel.send(finalMessage)
        }
    }

    fun deleteTransaction(transaction: edu.northeastern.expensetracker.domain.model.Transaction) {
        viewModelScope.launch {
            useCases.deleteTransaction(transaction)
        }
    }
}