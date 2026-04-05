package edu.northeastern.expensetracker.data.mapper

import edu.northeastern.expensetracker.data.local.entity.TransactionEntity
import edu.northeastern.expensetracker.domain.model.Transaction
import java.time.Instant
import java.time.ZoneId

// 1. Translates the Database Entity INTO the Domain Model
fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        categoryId = categoryId,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        exchangeRate = exchangeRate,
        baseAmount = baseAmount,
        // Converts the Long timestamp back into a usable LocalDate for the UI
        date = Instant.ofEpochMilli(dateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate(),
        notes = notes,
        isSynced = isSynced
    )
}

// 2. Translates the Domain Model INTO the Database Entity
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        categoryId = categoryId,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        exchangeRate = exchangeRate,
        baseAmount = baseAmount,
        // Converts the LocalDate into a Long timestamp so Room can save it
        dateTimestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        notes = notes,
        isSynced = isSynced
    )
}