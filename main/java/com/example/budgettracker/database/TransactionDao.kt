//Package: database
//file name: TransactionDao.kt

package com.example.budgettracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.budgettracker.models.Transaction

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // UPDATED: Filter by userId
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    // UPDATED: Filter by userId
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): LiveData<List<Transaction>>

    // UPDATED: Filter by userId
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncome(userId: String): LiveData<Double?>

    // UPDATED: Filter by userId
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpense(userId: String): LiveData<Double?>

    // UPDATED: Filter by userId
    @Query("SELECT * FROM transactions WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY date DESC")
    fun searchTransactions(userId: String, query: String): LiveData<List<Transaction>>

    // UPDATED: Delete only current user's transactions
    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactions(userId: String)
}