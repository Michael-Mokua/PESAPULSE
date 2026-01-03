package com.pesapulse.util

import com.pesapulse.data.model.TransactionEntity

object FinancialAdvisor {
    fun getAdvice(balance: java.math.BigDecimal, monthlyIncome: java.math.BigDecimal, monthlyExpenses: java.math.BigDecimal): String {
        val netSavings = monthlyIncome.subtract(monthlyExpenses)
        val savingsRate = if (monthlyIncome > java.math.BigDecimal.ZERO) {
            netSavings.divide(monthlyIncome, 2, java.math.RoundingMode.HALF_UP).toDouble()
        } else 0.0
        
        val ksh1000 = java.math.BigDecimal("1000")
        
        return when {
            balance < ksh1000 -> "Your balance is low (KES $balance). Avoid non-essential spending."
            savingsRate < 0.2 -> "Try to follow the 50/30/20 rule. You're currently saving ${(savingsRate * 100).toInt()}%."
            monthlyExpenses > monthlyIncome -> "Warning: Your spending exceeds your income this month!"
            else -> "Great job! You're on track with your savings goals."
        }
    }

    fun categorizeSpending(transactions: List<TransactionEntity>): Map<String, java.math.BigDecimal> {
        return transactions.filter { it.type in listOf("sent", "payment", "withdrawal") }
            .groupBy { it.category }
            .mapValues { entry -> 
                entry.value.map { it.amount }.fold(java.math.BigDecimal.ZERO) { acc, amt -> acc.add(amt) }
            }
    }
}
