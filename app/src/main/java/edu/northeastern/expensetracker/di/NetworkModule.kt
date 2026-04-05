package edu.northeastern.expensetracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.northeastern.expensetracker.data.remote.CurrencyApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCurrencyApi(): CurrencyApi {
        return Retrofit.Builder()
            .baseUrl(CurrencyApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Translates JSON to Kotlin
            .build()
            .create(CurrencyApi::class.java)
    }
}