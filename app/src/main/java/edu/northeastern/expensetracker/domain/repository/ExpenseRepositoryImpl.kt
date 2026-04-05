package edu.northeastern.expensetracker.data.repository

import edu.northeastern.expensetracker.data.local.dao.CategoryDao
import edu.northeastern.expensetracker.data.local.dao.TransactionDao
import edu.northeastern.expensetracker.data.remote.CurrencyApi
import edu.northeastern.expensetracker.data.mapper.*
import edu.northeastern.expensetracker.domain.model.Category
import edu.northeastern.expensetracker.domain.model.Transaction
import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// @Inject tells Hilt to automatically provide the DAOs when building this class
class ExpenseRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val api: CurrencyApi
) : ExpenseRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsBetween(start: LocalDate, end: LocalDate): Flow<List<Transaction>> {
        // Convert the clean LocalDates into Long timestamps for SQLite
        val startMillis = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        return transactionDao.getTransactionsBetweenDates(startMillis, endMillis).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    // <--- 2. ADD THE NETWORK CALL HERE
    override suspend fun getExchangeRates(baseCurrency: String): Map<String, Double> {
        return try {
            api.getExchangeRates(baseCurrency).rates
        } catch (e: Exception) {
            // If the phone has no internet, we catch the crash and return an empty map
            emptyMap()
        }
    }

    override suspend fun getUnsyncedTransactions(): List<edu.northeastern.expensetracker.domain.model.Transaction> {
        // Ask the DAO for Entities, then map them all to Domain models for the Worker to use
        return transactionDao.getUnsyncedTransactions().map { it.toDomain() }
        // Note: If your mapper function is named differently (e.g., .toDomainModel()), use that instead!
    }

    override suspend fun updateTransaction(transaction: edu.northeastern.expensetracker.domain.model.Transaction) {
        // The Worker hands us a Domain model. Translate it to an Entity before giving it to the DAO.
        transactionDao.updateTransaction(transaction.toEntity())
        // Note: Again, use your specific mapper name here (e.g., .toEntity())
    }
}