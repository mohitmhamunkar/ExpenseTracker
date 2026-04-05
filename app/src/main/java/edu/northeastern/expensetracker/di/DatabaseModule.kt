package edu.northeastern.expensetracker.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.northeastern.expensetracker.data.local.ExpenseDatabase
import edu.northeastern.expensetracker.data.local.dao.CategoryDao
import edu.northeastern.expensetracker.data.local.dao.TransactionDao
import javax.inject.Singleton

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
}