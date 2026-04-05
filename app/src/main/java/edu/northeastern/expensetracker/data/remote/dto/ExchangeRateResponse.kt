package edu.northeastern.expensetracker.data.remote.dto

// This class perfectly matches the JSON structure of the API
data class ExchangeRateResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double> // Maps currency codes (e.g., "USD" -> 83.50)
)