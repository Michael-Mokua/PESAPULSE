package com.pesapulse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val code: String,
    val amount: BigDecimal,
    val type: String, // deposit, withdrawal, payment, sent, received
    val category: String,
    val counterparty: String,
    val timestamp: Long,
    val balance: BigDecimal,
    val fulizaLimit: BigDecimal = BigDecimal.ZERO,
    val fulizaBalance: BigDecimal = BigDecimal.ZERO,
    val rawMessage: String
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val deadline: Long?
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val icon: String? = null
)
