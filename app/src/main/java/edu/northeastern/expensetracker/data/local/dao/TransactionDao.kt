package edu.northeastern.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import edu.northeastern.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity) // Removed : Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity) // Removed : Int

    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    // 1. Find all transactions that were saved offline (1:1 rate)
    // CHANGED: Returns List<TransactionEntity> instead of the Domain model
    @Query("SELECT * FROM transactions WHERE isSynced = 0 AND originalCurrency != 'INR'")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    // 2. Update a transaction after we get the real rate
    // CHANGED: Accepts TransactionEntity instead of the Domain model
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
}