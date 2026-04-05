package edu.northeastern.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.northeastern.expensetracker.data.local.dao.CategoryDao
import edu.northeastern.expensetracker.data.local.dao.TransactionDao
import edu.northeastern.expensetracker.data.local.entity.CategoryEntity
import edu.northeastern.expensetracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false // Set to true later if you want to track schema migrations
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao

    companion object {
        const val DATABASE_NAME = "expense_tracker_db"
    }
}