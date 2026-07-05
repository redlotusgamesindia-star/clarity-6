package com.runtimelabs.clarity.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.runtimelabs.clarity.domain.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fires per inexact alarm. Reads the habit fresh from the database — a habit
 * deleted or muted after the alarm was set drops silently instead of
 * resurrecting from stale extras — then posts and chains the next occurrence.
 */
@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var habitRepository: HabitRepository

    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_HABIT_REMINDER) return
        val habitId = intent.getLongExtra(ReminderScheduler.EXTRA_HABIT_ID, -1L)
        if (habitId < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val habit = habitRepository.getHabit(habitId)
                if (habit?.reminderMinutesOfDay != null) {
                    scheduler.showReminderNotification(habit.id, habit.name)
                    scheduler.schedule(habit) // chain the next occurrence
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
