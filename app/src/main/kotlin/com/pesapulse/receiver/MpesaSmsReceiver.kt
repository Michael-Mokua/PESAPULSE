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
import kotlinx.coroutines.withContext
import androidx.room.Room

class MpesaSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val pendingResult = goAsync()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    for (sms in messages) {
                        val sender = sms.displayOriginatingAddress
                        if (sender != null && sender.contains("MPESA", ignoreCase = true)) {
                            val body = sms.messageBody
                            val transaction = SmsParser.parse(body)
                            if (transaction != null) {
                                val database = AppDatabase.getDatabase(context)
                                database.transactionDao().insertTransaction(transaction)
                                withContext(Dispatchers.Main) {
                                    com.pesapulse.util.NotificationHelper.checkAlerts(context, transaction.balance, transaction.amount)
                                }
                            }
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

}
