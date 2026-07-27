package com.runtimelabs.clarity.data.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Habit reminders via INEXACT AlarmManager one-shots that re-chain on fire.
 *
 * Deliberately not exact alarms: USE_EXACT_ALARM is Play-restricted to
 * alarm-clock/calendar apps, SCHEDULE_EXACT_ALARM is user-revocable and
 * denied by default on 14+, and a habit nudge landing within Doze's batching
 * window (typically minutes) is exactly as useful. Deliberately not
 * WorkManager: its 15-minute periodic drift is wrong for "at 9:00 PM".
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timeCalculator: ReminderTimeCalculator,
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(habit: Habit) {
        val minuteOfDay = habit.reminderMinutesOfDay ?: run {
            cancel(habit.id)
            return
        }
        ensureChannel()
        val triggerAt = timeCalculator.nextTriggerAtMillis(
            daysMask = habit.daysMask,
            minuteOfDay = minuteOfDay,
            after = ZonedDateTime.now(),
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            reminderIntent(habit.id, createIfMissing = true)!!,
        )
    }

    fun cancel(habitId: Long) {
        reminderIntent(habitId, createIfMissing = false)?.let { pending ->
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    /** Safe to call anytime; permission and channel-mute are both respected. */
    fun showReminderNotification(habitId: Long, habitName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ensureChannel()

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // Copy rule (product decision): the title is the user's own habit name,
        // the body is neutral encouragement. Nothing clinical, nothing explicit.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(habitName)
            .setContentText(context.getString(R.string.reminder_notification_body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        manager.notify(habitId.toInt(), notification)
    }

    private fun reminderIntent(habitId: Long, createIfMissing: Boolean): PendingIntent? {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ACTION_HABIT_REMINDER
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val flags = if (createIfMissing) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, habitId.toInt(), intent, flags)
    }

    private fun ensureChannel() {
        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.reminder_channel_name))
                .setDescription(context.getString(R.string.reminder_channel_description))
                .build(),
        )
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val ACTION_HABIT_REMINDER = "com.runtimelabs.clarity.action.HABIT_REMINDER"
        const val EXTRA_HABIT_ID = "habit_id"
    }
}
