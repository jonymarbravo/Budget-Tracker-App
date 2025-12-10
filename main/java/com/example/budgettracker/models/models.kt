//Package: models
//file name: models.kt

package com.example.budgettracker.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",  // ADD THIS - to separate transactions by user
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class CategoryTotal(
    val category: String,
    val total: Double,
    val percentage: Float
)

// Savings Goal Model
@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)