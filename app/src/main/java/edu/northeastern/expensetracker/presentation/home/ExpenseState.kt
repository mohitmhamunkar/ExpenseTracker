package edu.northeastern.expensetracker.presentation.home

import edu.northeastern.expensetracker.domain.model.Transaction

data class ExpenseState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)