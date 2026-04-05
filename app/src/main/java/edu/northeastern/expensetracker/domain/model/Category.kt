package edu.northeastern.expensetracker.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String // We will use this later to load Material Icons dynamically
)
