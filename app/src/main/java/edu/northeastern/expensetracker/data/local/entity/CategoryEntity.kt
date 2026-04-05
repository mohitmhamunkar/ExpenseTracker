package edu.northeastern.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val type: String, // We store the Enum as a String in the database
    val iconName: String
)