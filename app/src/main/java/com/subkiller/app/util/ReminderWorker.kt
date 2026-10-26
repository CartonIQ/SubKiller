package com.subkiller.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.subkiller.app.R
import com.subkiller.app.data.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Daily 9 AM background worker:
 * - If nextPaymentDate is exactly 2 days away: "Heads up! Your [Name] subscription of $[Price] will charge in 2 days."
 * - If Free Trial ends in 1 day: "⚠️ Your free trial for [Name] ends tomorrow! Cancel now to avoid being charged."
 */
class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "subkiller_payment_reminders"
        const val CHANNEL_NAME = "Subscription Reminders"
    }

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(context)
            val subscriptions = database.subscriptionDao().getActiveSubscriptionsSync()
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            createNotificationChannel()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            subscriptions.forEach { sub ->
                try {
                    val paymentDate = LocalDate.parse(sub.nextPaymentDate, formatter)
                    val daysUntil = ChronoUnit.DAYS.between(today, paymentDate)

                    // Rule 1: Free Trial ending in exactly 1 day
                    if (sub.isFreeTrial && daysUntil == 1L) {
                        val title = "⚠️ Free Trial Ending Tomorrow!"
                        val message = "⚠️ Your free trial for ${sub.name} ends tomorrow! Cancel now to avoid being charged."
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_warning)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build()

                        notificationManager.notify((sub.id * 10 + 1).toInt(), notification)
                    }

                    // Rule 2: nextPaymentDate exactly 2 days away
                    if (daysUntil == 2L) {
                        val title = "Upcoming Payment Due"
                        val priceFormatted = String.format("%.2f", sub.price)
                        val message = "Heads up! Your ${sub.name} subscription of $${priceFormatted} will charge in 2 days."
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_reminders)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .build()

                        notificationManager.notify((sub.id * 10 + 2).toInt(), notification)
                    }
                } catch (e: Exception) {
                    // Ignore date parse errors for invalid legacy entries
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Reminders for upcoming charges and expiring free trials"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
