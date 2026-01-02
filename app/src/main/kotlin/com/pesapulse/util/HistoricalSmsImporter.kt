package com.pesapulse.util

import android.content.Context
import android.provider.Telephony
import com.pesapulse.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object HistoricalSmsImporter {
    suspend fun importLast6Months(context: Context, database: AppDatabase) {
        withContext(Dispatchers.IO) {
            val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE)
            val selection = "${Telephony.Sms.ADDRESS} LIKE '%MPESA%'"
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex)
                    val transaction = SmsParser.parse(body)
                    if (transaction != null) {
                        database.transactionDao().insertTransaction(transaction)
                    }
                }
            }
        }
    }
}
