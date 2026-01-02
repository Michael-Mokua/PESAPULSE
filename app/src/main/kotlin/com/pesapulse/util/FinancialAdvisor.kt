package com.pesapulse.util

import com.pesapulse.data.model.TransactionEntity

object FinancialAdvisor {
    fun getAdvice(balance: Double, monthlyIncome: Double, monthlyExpenses: Double): String {
        val savingsRate = if (monthlyIncome > 0) (monthlyIncome - monthlyExpenses) / monthlyIncome else 0.0
        
        return when {
            balance < 1000 -> "Your balance is low (KES $balance). Avoid non-essential spending."
            savingsRate < 0.2 -> "Try to follow the 50/30/20 rule. You're currently saving ${(savingsRate * 100).toInt()}%."
            monthlyExpenses > monthlyIncome -> "Warning: Your spending exceeds your income this month!"
            else -> "Great job! You're on track with your savings goals."
        }
    }

    fun categorizeSpending(transactions: List<TransactionEntity>): Map<String, Double> {
        return transactions.filter { it.type in listOf("sent", "payment", "withdrawal") }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
