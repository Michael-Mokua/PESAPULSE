package com.pesapulse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val amount: Double,
    val type: String, // deposit, withdrawal, payment, sent, received
    val category: String,
    val counterparty: String,
    val timestamp: Long,
    val balance: Double,
    val rawMessage: String
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Long?
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val icon: String? = null
)
