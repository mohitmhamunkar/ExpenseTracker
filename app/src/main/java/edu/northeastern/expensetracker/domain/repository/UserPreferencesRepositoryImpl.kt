package edu.northeastern.expensetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import edu.northeastern.expensetracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    // 1. Define a "Key" to store the data under (like a dictionary key)
    private val HOME_CURRENCY_KEY = stringPreferencesKey("home_currency")

    // 2. Read the data. If it doesn't exist yet, default to "INR"
    override val homeCurrency: Flow<String> = dataStore.data.map { preferences ->
        preferences[HOME_CURRENCY_KEY] ?: "INR"
    }

    // 3. Write the data
    override suspend fun saveHomeCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[HOME_CURRENCY_KEY] = currency
        }
    }
}