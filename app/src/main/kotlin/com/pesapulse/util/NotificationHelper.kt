package com.pesapulse.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pesapulse.R

object NotificationHelper {
    private const val CHANNEL_ID = "pesapulse_alerts"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Financial Alerts", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun checkAlerts(context: Context, balance: Double, amount: Double) {
        if (balance < 1000) {
            showNotification(context, "Low Balance Alert", "Your M-Pesa balance is below KES 1,000.")
        }
        if (amount > 10000) {
            showNotification(context, "Large Transaction Detected", "A transaction of KES $amount was just recorded.")
        }
    }
}
