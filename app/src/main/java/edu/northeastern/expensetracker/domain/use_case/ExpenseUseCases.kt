package edu.northeastern.expensetracker.domain.use_case

import javax.inject.Inject

// This acts as a single container for all your app's actions
data class ExpenseUseCases @Inject constructor(
    val getTransactions: GetTransactionsUseCase,
    val addTransaction: AddTransactionUseCase,
    val deleteTransaction: DeleteTransaction,
    val getExchangeRates: GetExchangeRates
)