package edu.northeastern.expensetracker.domain.use_case

import edu.northeastern.expensetracker.domain.model.Transaction
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        // Later, we can add sorting logic here (e.g., sorting by date or amount)
        // without ever touching the UI or the Database!
        return repository.getAllTransactions()
    }
}