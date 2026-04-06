package edu.northeastern.expensetracker.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.northeastern.expensetracker.data.local.ExpenseDatabase
import edu.northeastern.expensetracker.data.local.dao.CategoryDao
import edu.northeastern.expensetracker.data.local.dao.TransactionDao
import edu.northeastern.expensetracker.data.repository.UserPreferencesRepositoryImpl
import edu.northeastern.expensetracker.domain.repository.UserPreferencesRepository
import javax.inject.Singleton

// 1. Create the actual DataStore file on the phone (Outside the object!)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(app: Application): ExpenseDatabase {
        return Room.databaseBuilder(
            app,
            ExpenseDatabase::class.java,
            ExpenseDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: ExpenseDatabase): TransactionDao {
        return db.transactionDao
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: ExpenseDatabase): CategoryDao {
        return db.categoryDao
    }

    // --- NEW DATASTORE PROVIDERS ---

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }
}