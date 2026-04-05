package edu.northeastern.expensetracker.domain.use_case

import edu.northeastern.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetExchangeRates @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(baseCurrency: String): Map<String, Double> {
        return repository.getExchangeRates(baseCurrency)
    }
}