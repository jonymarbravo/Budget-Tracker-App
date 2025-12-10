//Package: viewmodels
//file name: TransactionViewModel.kt

package com.example.budgettracker.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.database.AppDatabase
import com.example.budgettracker.models.Transaction
import com.example.budgettracker.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Get current user ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Use switchMap to update data when user changes
    private val userIdLiveData = MutableLiveData<String>()

    val allTransactions: LiveData<List<Transaction>>
    val totalIncome: LiveData<Double?>
    val totalExpense: LiveData<Double?>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)

        // Set current user ID
        userIdLiveData.value = currentUserId

        // Observe user-specific data
        allTransactions = userIdLiveData.switchMap { userId ->
            repository.getAllTransactions(userId)
        }

        totalIncome = userIdLiveData.switchMap { userId ->
            repository.getTotalIncome(userId)
        }

        totalExpense = userIdLiveData.switchMap { userId ->
            repository.getTotalExpense(userId)
        }
    }

    // Refresh data for current user
    fun refreshData() {
        userIdLiveData.value = currentUserId
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        // Add current user ID to transaction
        val transactionWithUser = transaction.copy(userId = currentUserId)
        repository.insert(transactionWithUser)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        // Ensure transaction belongs to current user
        val transactionWithUser = transaction.copy(userId = currentUserId)
        repository.update(transactionWithUser)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun searchTransactions(query: String): LiveData<List<Transaction>> {
        return repository.searchTransactions(currentUserId, query)
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByDateRange(currentUserId, startDate, endDate)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return repository.getTransactionById(id)
    }
}