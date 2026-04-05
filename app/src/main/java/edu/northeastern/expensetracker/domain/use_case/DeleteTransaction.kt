package edu.northeastern.expensetracker.domain.use_case

import edu.northeastern.expensetracker.domain.model.Transaction
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteTransaction @Inject constructor(
    private val repository: ExpenseRepository
) {
    // The 'operator fun invoke' allows us to call this class like a function in the ViewModel
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
}