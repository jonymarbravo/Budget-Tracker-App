//Package: repository
//TransactionRepository.kt

package com.example.budgettracker.repository

import androidx.lifecycle.LiveData
import com.example.budgettracker.database.TransactionDao
import com.example.budgettracker.models.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(userId: String): LiveData<List<Transaction>> =
        transactionDao.getAllTransactions(userId)

    fun getTotalIncome(userId: String): LiveData<Double?> =
        transactionDao.getTotalIncome(userId)

    fun getTotalExpense(userId: String): LiveData<Double?> =
        transactionDao.getTotalExpense(userId)

    suspend fun insert(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)
    suspend fun getTransactionById(id: Long) = transactionDao.getTransactionById(id)

    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long) =
        transactionDao.getTransactionsByDateRange(userId, startDate, endDate)

    fun searchTransactions(userId: String, query: String) =
        transactionDao.searchTransactions(userId, query)

    suspend fun deleteAllTransactions(userId: String) =
        transactionDao.deleteAllTransactions(userId)
}