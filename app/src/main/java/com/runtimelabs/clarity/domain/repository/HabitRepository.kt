package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.Habit
import com.runtimelabs.clarity.domain.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    /** All habits, oldest first. */
    fun observeHabits(): Flow<List<Habit>>

    fun observeCompletionsSince(sinceEpochDay: Long): Flow<List<HabitCompletion>>

    suspend fun getHabit(id: Long): Habit?

    /**
     * Insert or update, then reconcile the reminder alarm (schedule when set,
     * cancel when cleared). Returns the persisted id.
     */
    suspend fun saveHabit(habit: Habit): Long

    /** Cascades completions and cancels any reminder. */
    suspend fun deleteHabit(id: Long)

    suspend fun setCompleted(habitId: Long, epochDay: Long, completed: Boolean)

    /** Re-arms every enabled reminder (boot, app start, post-fire chaining). */
    suspend fun rescheduleAllReminders()
}
