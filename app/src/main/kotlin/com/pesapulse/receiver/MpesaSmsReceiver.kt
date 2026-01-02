package com.pesapulse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.pesapulse.data.local.AppDatabase
import com.pesapulse.util.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.Room

class MpesaSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                if (sender != null && sender.contains("MPESA", ignoreCase = true)) {
                    val body = sms.messageBody
                    val transaction = SmsParser.parse(body)
                    if (transaction != null) {
                        saveTransaction(context, transaction)
                    }
                }
            }
        }
    }

    private fun saveTransaction(context: Context, transaction: com.pesapulse.data.model.TransactionEntity) {
        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "pesapulse-db"
        ).build()

        CoroutineScope(Dispatchers.IO).launch {
            database.transactionDao().insertTransaction(transaction)
            // Trigger a notification if balance is low or transaction is large
            com.pesapulse.util.NotificationHelper.checkAlerts(context, transaction.balance, transaction.amount)
        }
    }
}
