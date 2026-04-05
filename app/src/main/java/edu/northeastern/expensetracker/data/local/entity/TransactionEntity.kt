package edu.northeastern.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val categoryId: String,
    val originalAmount: Double,
    val originalCurrency: String,
    val exchangeRate: Double,
    val baseAmount: Double,
    val dateTimestamp: Long, // Converted from LocalDate for fast DB sorting
    val notes: String,
    val isSynced: Boolean
)