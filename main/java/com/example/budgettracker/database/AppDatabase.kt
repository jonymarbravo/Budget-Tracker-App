//Package: database
//File name: AppDatabase.kt

package com.example.budgettracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgettracker.models.Transaction

@Database(entities = [Transaction::class], version = 2, exportSchema = false)  // Changed to version 2
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker_database"
                )
                    .fallbackToDestructiveMigration()  // This will recreate database
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}