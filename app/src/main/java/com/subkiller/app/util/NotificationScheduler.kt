package com.subkiller.app.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Schedules WorkManager to run daily at 9:00 AM.
 */
object NotificationScheduler {
    private const val WORK_NAME = "subkiller_daily_9am_reminder"

    fun scheduleDailyReminder(context: Context) {
        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(9, 0, 0))
        if (now.isAfter(target)) {
            target = target.plusDays(1)
        }
        val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(0)

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
