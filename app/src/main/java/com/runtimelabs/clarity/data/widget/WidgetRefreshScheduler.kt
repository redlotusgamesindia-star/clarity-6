package com.runtimelabs.clarity.data.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.runtimelabs.clarity.data.notifications.ReminderTimeCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arms a single, self-chaining inexact alarm that fires shortly after local
 * midnight so the widget reflects the new day even if the app is never
 * opened. Same rationale as habit reminders (§18): USE_EXACT_ALARM is
 * Play-restricted, WorkManager's periodic drift is the wrong shape for "at
 * a specific local time" — and reuses the exact same tested trigger-time
 * math ([ReminderTimeCalculator]) rather than reinventing it.
 */
@Singleton
class WidgetRefreshScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timeCalculator: ReminderTimeCalculator,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNext() {
        val triggerAt = timeCalculator.nextTriggerAtMillis(
            daysMask = EVERY_DAY_MASK,
            minuteOfDay = REFRESH_MINUTE_OF_DAY,
            after = ZonedDateTime.now(),
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent())
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, WidgetRefreshReceiver::class.java).apply {
            action = ACTION_WIDGET_REFRESH
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.runtimelabs.clarity.action.WIDGET_REFRESH"
        private const val REQUEST_CODE = 9001
        private const val REFRESH_MINUTE_OF_DAY = 2 // 00:02 local — a hair past midnight
        private const val EVERY_DAY_MASK = 0b1111111 // every weekday; not habit-specific, so not borrowed from Habit
    }
}
