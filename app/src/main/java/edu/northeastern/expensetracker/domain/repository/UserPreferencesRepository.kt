package edu.northeastern.expensetracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    // A Flow that constantly emits the current currency whenever it changes
    val homeCurrency: Flow<String>

    // A function to save the new currency
    suspend fun saveHomeCurrency(currency: String)
}