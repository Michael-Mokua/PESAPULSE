package com.pesapulse.util

import com.pesapulse.data.model.TransactionEntity
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object SmsParser {
    // Contextual Anchoring Patterns
    private val CODE_PATTERN = Pattern.compile("([A-Z0-9]{10})[\\s\\n]Confirmed", Pattern.CASE_INSENSITIVE)
    
    // User Provided Pattern for Precision
    private val AMOUNT_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([\\d,]+\\.\\d{2})\\s*(sent|received|paid|withdrawn|deposited|bought|withdrawn)", Pattern.CASE_INSENSITIVE)
    private val BALANCE_PATTERN = Pattern.compile("(?:New M-PESA balance is|balance was|account balance is)\\s?(?:Ksh|KES)\\s*([\\d,]+\\.\\d{2})", Pattern.CASE_INSENSITIVE)
    
    // Detailed Extraction
    private val SENT_TO_PATTERN = Pattern.compile("sent to (.*?) on", Pattern.CASE_INSENSITIVE)
    private val RECEIVED_FROM_PATTERN = Pattern.compile("received .* from (.*?) on", Pattern.CASE_INSENSITIVE)
    private val PAID_TO_PATTERN = Pattern.compile("paid to (.*?) (?:on|for account)", Pattern.CASE_INSENSITIVE)
    private val WITHDRAW_FROM_PATTERN = Pattern.compile("withdrawn from (.*?) on", Pattern.CASE_INSENSITIVE)

    private val FULIZA_LIMIT_PATTERN = Pattern.compile("(?:Fuliza limit is|limit is)\\s?(?:Ksh|KES)\\s*([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE)
    private val FULIZA_BAL_PATTERN = Pattern.compile("(?:Outstanding Fuliza balance is|Fuliza balance is)\\s?(?:Ksh|KES)\\s*([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE)

    fun parse(message: String): TransactionEntity? {
        try {
            val codeMatcher = CODE_PATTERN.matcher(message)
            if (!codeMatcher.find()) return null
            val code = codeMatcher.group(1) ?: return null

            val amountMatcher = AMOUNT_PATTERN.matcher(message)
            var amount = BigDecimal.ZERO
            var type = "other"
            if (amountMatcher.find()) {
                amount = parseBigDecimal(amountMatcher.group(1))
                type = amountMatcher.group(2)?.lowercase() ?: "other"
            }

            val balanceMatcher = BALANCE_PATTERN.matcher(message)
            val balance = if (balanceMatcher.find()) parseBigDecimal(balanceMatcher.group(1)) else BigDecimal.ZERO

            val limitMatcher = FULIZA_LIMIT_PATTERN.matcher(message)
            val fulizaLimit = if (limitMatcher.find()) parseBigDecimal(limitMatcher.group(1)) else BigDecimal.ZERO

            val fBalMatcher = FULIZA_BAL_PATTERN.matcher(message)
            val fulizaBalance = if (fBalMatcher.find()) parseBigDecimal(fBalMatcher.group(1)) else BigDecimal.ZERO

            // Normalizing type names
            type = when (type) {
                "withdrawn" -> "withdrawal"
                "paid", "bought" -> "payment"
                else -> type
            }

            val counterparty = inferCounterparty(message, type)
            val category = inferCategory(counterparty)

            val timePat = Pattern.compile("on (\\d{1,2}[/\\.\\-]\\d{1,2}[/\\.\\-]\\d{2,4}) at (\\d{1,2}:\\d{2}\\s?[AP]M)", Pattern.CASE_INSENSITIVE)
            val tm = timePat.matcher(message)
            val timestamp = if (tm.find()) parseDateTime(tm.group(1), tm.group(2)) else System.currentTimeMillis()

            return TransactionEntity(
                code = code,
                amount = amount,
                type = type,
                category = category,
                counterparty = counterparty,
                timestamp = timestamp,
                balance = balance,
                fulizaLimit = fulizaLimit,
                fulizaBalance = fulizaBalance,
                rawMessage = message
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseBigDecimal(value: String?): BigDecimal {
        return try {
            BigDecimal(value?.replace(",", "") ?: "0.00")
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun parseDateTime(date: String?, time: String?): Long {
        if (date == null || time == null) return System.currentTimeMillis()
        val cleanTime = time.replace(" ", "").uppercase()
        val cleanDate = date.replace(".", "/").replace("-", "/")
        val formats = arrayOf("dd/MM/yy hh:mma", "dd/MM/yyyy hh:mma", "d/M/yy h:mma", "d/M/yyyy h:mma")
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                return sdf.parse("$cleanDate $cleanTime")?.time ?: continue
            } catch (e: Exception) {}
        }
        return System.currentTimeMillis()
    }

    private fun inferCounterparty(message: String, type: String): String {
        return when (type) {
            "sent" -> findMatch(SENT_TO_PATTERN, message)
            "received" -> findMatch(RECEIVED_FROM_PATTERN, message)
            "payment" -> findMatch(PAID_TO_PATTERN, message)
            "withdrawal" -> findMatch(WITHDRAW_FROM_PATTERN, message)
            else -> "M-PESA"
        }
    }

    private fun findMatch(pattern: Pattern, text: String): String {
        val m = pattern.matcher(text)
        return if (m.find()) m.group(1)?.trim() ?: "Unknown" else "Unknown"
    }

    private fun inferCategory(counterparty: String): String {
        val cp = counterparty.uppercase()
        return when {
            cp.contains("KPLC") || cp.contains("ZUKU") || cp.contains("WATER") -> "Utilities"
            cp.contains("NAIVAS") || cp.contains("CARREFOUR") || cp.contains("QUICKMART") -> "Groceries"
            cp.contains("RESTAURANT") || cp.contains("CAFE") || cp.contains("JOEYS") -> "Dining"
            cp.contains("JUMIA") || cp.contains("AMAZON") || cp.contains("SHOP") -> "Shopping"
            cp.contains("EQUITY") || cp.contains("KCB") || cp.contains("COOP") -> "Bank"
            cp.contains("Safaricom") || cp.contains("AIRTIME") -> "Airtime"
            else -> "General"
        }
    }
}
