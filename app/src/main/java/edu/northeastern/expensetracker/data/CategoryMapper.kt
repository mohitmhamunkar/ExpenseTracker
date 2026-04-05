package edu.northeastern.expensetracker.data.mapper

import edu.northeastern.expensetracker.data.local.entity.CategoryEntity
import edu.northeastern.expensetracker.domain.model.Category
import edu.northeastern.expensetracker.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = TransactionType.valueOf(type), // Converts String back to Enum
        iconName = iconName
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.name, // Converts Enum to a safe String for the DB
        iconName = iconName
    )
}