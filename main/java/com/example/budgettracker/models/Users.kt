//Package: models
//file name: Users.kt


package com.example.budgettracker.models

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)