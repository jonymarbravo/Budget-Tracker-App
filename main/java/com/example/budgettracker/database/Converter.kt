//Package: database
// FILE 1: Converters.kt
package com.example.budgettracker.database

import androidx.room.TypeConverter
import com.example.budgettracker.models.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}