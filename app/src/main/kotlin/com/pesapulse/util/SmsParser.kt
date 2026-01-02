package com.pesapulse.util

import com.pesapulse.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object SmsParser {
    private val CODE_PATTERN = Pattern.compile("^([A-Z0-9]{10}) Confirmed\\.")
    private val AMOUNT_PATTERN = Pattern.compile("Ksh([\\d,]+\\.\\d{2})")
    private val BALANCE_PATTERN = Pattern.compile("New M-PESA balance is Ksh([\\d,]+\\.\\d{2})")
    private val DATE_TIME_PATTERN = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2}) at (\\d{1,2}:\\d{2} [AP]M)")

    fun parse(message: String): TransactionEntity? {
        try {
            val codeMatcher = CODE_PATTERN.matcher(message)
            if (!codeMatcher.find()) return null
            val code = codeMatcher.group(1) ?: return null

            val amountMatcher = AMOUNT_PATTERN.matcher(message)
            if (!amountMatcher.find()) return null
            val amountStr = amountMatcher.group(1)?.replace(",", "") ?: "0.0"
            val amount = amountStr.toDouble()

            val balanceMatcher = BALANCE_PATTERN.matcher(message)
            val balance = if (balanceMatcher.find()) {
                balanceMatcher.group(1)?.replace(",", "")?.toDouble() ?: 0.0
            } else 0.0

            val dateTimeMatcher = DATE_TIME_PATTERN.matcher(message)
            val timestamp = if (dateTimeMatcher.find()) {
                val dateStr = dateTimeMatcher.group(1)
                val timeStr = dateTimeMatcher.group(2)
                parseDateTime(dateStr, timeStr)
            } else System.currentTimeMillis()

            val type = inferType(message)
            val counterparty = inferCounterparty(message, type)
            val category = inferCategory(counterparty)

            return TransactionEntity(
                code = code,
                amount = amount,
                type = type,
                category = category,
                counterparty = counterparty,
                timestamp = timestamp,
                balance = balance,
                rawMessage = message
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseDateTime(date: String?, time: String?): Long {
        if (date == null || time == null) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.ENGLISH)
            sdf.parse("$date $time")?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun inferType(message: String): String {
        return when {
            message.contains("received", ignoreCase = true) -> "received"
            message.contains("sent", ignoreCase = true) -> "sent"
            message.contains("paid", ignoreCase = true) -> "payment"
            message.contains("Withdraw", ignoreCase = true) -> "withdrawal"
            message.contains("deposited", ignoreCase = true) -> "deposit"
            else -> "other"
        }
    }

    private fun inferCounterparty(message: String, type: String): String {
        return when (type) {
            "received" -> extractBetween(message, "from ", " on")
            "sent" -> extractBetween(message, "to ", " on")
            "payment" -> extractBetween(message, "paid to ", " on")
            else -> "M-PESA"
        }
    }

    private fun extractBetween(text: String, start: String, end: String): String {
        val startIndex = text.indexOf(start)
        if (startIndex == -1) return "Unknown"
        val actualStart = startIndex + start.length
        val endIndex = text.indexOf(end, actualStart)
        if (endIndex == -1) return text.substring(actualStart).trim()
        return text.substring(actualStart, endIndex).trim()
    }

    private fun inferCategory(counterparty: String): String {
        val cp = counterparty.uppercase()
        return when {
            cp.contains("KPLC") || cp.contains("ZUKU") || cp.contains("WATER") -> "Utilities"
            cp.contains("SUPERMARKET") || cp.contains("NAIVAS") || cp.contains("CARREFOUR") -> "Groceries"
            cp.contains("RESTAURANT") || cp.contains("CAFE") || cp.contains("FOOD") -> "Dining"
            cp.contains("JUMIA") || cp.contains("AMAZON") -> "Shopping"
            cp.contains("EQUITY") || cp.contains("KCB") || cp.contains("COOP") -> "Bank Transfer"
            else -> "General"
        }
    }
}
