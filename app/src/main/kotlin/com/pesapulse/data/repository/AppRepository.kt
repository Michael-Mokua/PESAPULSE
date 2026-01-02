package com.pesapulse.data.repository

import com.pesapulse.data.local.TransactionDao
import com.pesapulse.data.local.GoalDao
import com.pesapulse.data.model.TransactionEntity
import com.pesapulse.data.model.GoalEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val transactionDao: TransactionDao,
    private val goalDao: GoalDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val latestTransaction: Flow<TransactionEntity?> = transactionDao.getLatestTransaction()

    fun getTotalIncome(startTime: Long) = transactionDao.getTotalIncome(startTime)
    fun getTotalExpenses(startTime: Long) = transactionDao.getTotalExpenses(startTime)

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun insertGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }
}
