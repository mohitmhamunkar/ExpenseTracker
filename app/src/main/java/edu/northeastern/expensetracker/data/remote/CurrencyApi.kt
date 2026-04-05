package edu.northeastern.expensetracker.data.remote

import edu.northeastern.expensetracker.data.remote.dto.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {

    // The {baseCurrency} lets us dynamically ask for rates. We will default to INR.
    @GET("v4/latest/{baseCurrency}")
    suspend fun getExchangeRates(
        @Path("baseCurrency") baseCurrency: String = "INR"
    ): ExchangeRateResponse

    companion object {
        // The root URL for the free exchange rate API
        const val BASE_URL = "https://api.exchangerate-api.com/"
    }
}