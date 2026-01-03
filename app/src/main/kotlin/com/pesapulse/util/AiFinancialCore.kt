package com.pesapulse.util

import com.pesapulse.data.model.TransactionEntity
import java.util.*

object AiFinancialCore {

    fun predictBalance(history: List<TransactionEntity>, daysAhead: Int = 30): List<Pair<Long, java.math.BigDecimal>> {
        if (history.isEmpty()) return emptyList()
        
        val lastBalance = history.first().balance
        val dailyAverages = history.filter { it.type in listOf("sent", "payment", "withdrawal") }
            .groupBy { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.DAY_OF_YEAR)
            }
            .mapValues { entry -> 
                entry.value.map { it.amount }.fold(java.math.BigDecimal.ZERO) { acc, amt -> acc.add(amt) }
            }
        
        val totalDays = dailyAverages.size
        val avgDailySpend = if (totalDays > 0) {
            dailyAverages.values.fold(java.math.BigDecimal.ZERO) { acc, amt -> acc.add(amt) }
                .divide(java.math.BigDecimal(totalDays), 2, java.math.RoundingMode.HALF_UP)
        } else java.math.BigDecimal.ZERO
        
        val predictions = mutableListOf<Pair<Long, java.math.BigDecimal>>()
        val now = System.currentTimeMillis()
        
        for (i in 0 until daysAhead) {
            val futureTime = now + (i * 24 * 60 * 60 * 1000L)
            val totalReduction = avgDailySpend.multiply(java.math.BigDecimal(i))
            val predictedBal = lastBalance.subtract(totalReduction).max(java.math.BigDecimal.ZERO)
            predictions.add(futureTime to predictedBal)
        }
        
        return predictions
    }

    fun detectSubscriptions(history: List<TransactionEntity>): List<String> {
        return history.filter { it.type == "payment" }
            .groupBy { it.counterparty }
            .filter { entry -> 
                val timestamps = entry.value.map { it.timestamp }.sorted()
                if (timestamps.size < 2) return@filter false
                
                // Check for roughly monthly intervals (25-35 days)
                var monthlyCount = 0
                for (i in 0 until timestamps.size - 1) {
                    val diff = timestamps[i+1] - timestamps[i]
                    val days = diff / (1000 * 60 * 60 * 24)
                    if (days in 25..35) monthlyCount++
                }
                monthlyCount >= 1
            }
            .keys.toList()
    }

    fun getFrequentCounterparties(history: List<TransactionEntity>, limit: Int = 5): List<Triple<String, java.math.BigDecimal, Int>> {
        return history.groupBy { it.counterparty }
            .map { (name, txs) -> 
                val flow = txs.fold(java.math.BigDecimal.ZERO) { acc, it ->
                    if (it.type in listOf("received", "deposit")) acc.add(it.amount) else acc.subtract(it.amount)
                }
                Triple(name, flow, txs.size)
            }
            .filter { it.first != "M-PESA" && it.first != "Unknown" }
            .sortedByDescending { it.third }
            .take(limit)
    }
}
