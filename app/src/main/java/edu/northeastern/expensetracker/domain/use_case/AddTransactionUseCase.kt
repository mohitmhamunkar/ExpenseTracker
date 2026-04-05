package edu.northeastern.expensetracker.domain.use_case

import edu.northeastern.expensetracker.domain.model.Transaction
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        // Business Logic Example:
        // if (transaction.baseAmount <= 0.0) throw Exception("Amount must be greater than zero")

        repository.insertTransaction(transaction)
    }
}