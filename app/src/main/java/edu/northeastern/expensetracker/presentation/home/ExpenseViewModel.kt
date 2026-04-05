package edu.northeastern.expensetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.northeastern.expensetracker.domain.model.Transaction
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val useCases: ExpenseUseCases
) : ViewModel() {

    // This is the private pipeline where we update the data
    private val _state = MutableStateFlow(ExpenseState())
    // This is the public pipeline that the UI watches
    val state: StateFlow<ExpenseState> = _state.asStateFlow()
    // The user's anchor currency (can be updated from a Settings screen later)
    var userHomeCurrency = "INR"
    private val eventChannel = Channel<String>()
    val uiEvent = eventChannel.receiveAsFlow()

    init {
        // The second the ViewModel is born, we tell it to fetch the transactions
        getTransactions()
    }

    private fun getTransactions() {
        // We use the UseCase like a function!
        // .onEach means "Every time the database changes, update the UI state"
        useCases.getTransactions().onEach { newTransactions ->
            _state.value = state.value.copy(
                transactions = newTransactions
            )
        }.launchIn(viewModelScope)
    }

    // Replaces your old addTransaction function to accept the raw form data
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

            // 1. Create a dynamic flag, assuming success by default (e.g., for INR transactions)
            var wasSuccessfullySynced = true

            if (selectedCurrency != userHomeCurrency) {
                val rates = useCases.getExchangeRates(selectedCurrency)
                val conversionRate = rates[userHomeCurrency]

                if (conversionRate != null) {
                    finalRate = conversionRate
                    finalBaseAmount = amount * conversionRate
                    // It worked online! Leave the flag as true.
                } else {
                    finalMessage = "Offline: Saved using 1:1 rate for $selectedCurrency"
                    // 2. It failed! Mark it so the Worker can find it later.
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

                // 3. Pass the dynamic flag to the database
                isSynced = wasSuccessfullySynced
            )

            useCases.addTransaction(newTransaction)
            eventChannel.send(finalMessage)
        }
    }

    fun deleteTransaction(transaction: edu.northeastern.expensetracker.domain.model.Transaction) {
        viewModelScope.launch {
            // Replace 'dao' with whatever you named your Room DAO variable
            useCases.deleteTransaction(transaction)
        }
    }
}