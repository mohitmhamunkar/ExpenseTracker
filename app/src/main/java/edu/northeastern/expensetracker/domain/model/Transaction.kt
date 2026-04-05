package edu.northeastern.expensetracker.domain.model

import java.time.LocalDate

data class Transaction(
    val id: String,
    val categoryId: String,
    val originalAmount: Double,
    val originalCurrency: String,
    val exchangeRate: Double,
    val baseAmount: Double, // The converted amount in your primary currency (e.g., USD)
    val date: LocalDate,
    val notes: String,
    val isSynced: Boolean // Crucial for our offline-first architecture
)