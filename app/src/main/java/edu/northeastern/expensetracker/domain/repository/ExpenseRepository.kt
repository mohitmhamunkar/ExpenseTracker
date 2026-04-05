package edu.northeastern.expensetracker.domain.repository

import edu.northeastern.expensetracker.domain.model.Category
import edu.northeastern.expensetracker.domain.model.Transaction
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsBetween(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    // Categories
    fun getAllCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category)
    // Returns a map of currency codes and their rates (e.g., "USD" -> 83.50)
    suspend fun getExchangeRates(baseCurrency: String): Map<String, Double>
    suspend fun getUnsyncedTransactions(): List<edu.northeastern.expensetracker.domain.model.Transaction>
    suspend fun updateTransaction(transaction: edu.northeastern.expensetracker.domain.model.Transaction)
}