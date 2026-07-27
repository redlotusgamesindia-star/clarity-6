package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.HabitCompletionDao
import com.runtimelabs.clarity.data.local.db.dao.HabitDao
import com.runtimelabs.clarity.data.local.db.entity.HabitCompletionEntity
import com.runtimelabs.clarity.data.local.db.entity.HabitEntity
import com.runtimelabs.clarity.data.notifications.ReminderScheduler
import com.runtimelabs.clarity.domain.model.Habit
import com.runtimelabs.clarity.domain.model.HabitCompletion
import com.runtimelabs.clarity.domain.repository.HabitRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val scheduler: ReminderScheduler,
) : HabitRepository {

    override fun observeHabits(): Flow<List<Habit>> =
        habitDao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun observeCompletionsSince(sinceEpochDay: Long): Flow<List<HabitCompletion>> =
        completionDao.observeSince(sinceEpochDay)
            .map { rows -> rows.map { HabitCompletion(habitId = it.habitId, epochDay = it.epochDay) } }
            .distinctUntilChanged()

    override suspend fun getHabit(id: Long): Habit? = habitDao.getById(id)?.toDomain()

    override suspend fun saveHabit(habit: Habit): Long {
        val persistedId = if (habit.id == Habit.NEW_ID) {
            habitDao.insert(habit.toEntity(id = 0))
        } else {
            habitDao.update(habit.toEntity(id = habit.id))
            habit.id
        }
        val persisted = habit.copy(id = persistedId)
        if (persisted.reminderMinutesOfDay != null) {
            scheduler.schedule(persisted)
        } else {
            scheduler.cancel(persistedId)
        }
        return persistedId
    }

    override suspend fun deleteHabit(id: Long) {
        scheduler.cancel(id)
        habitDao.deleteWithCompletions(id)
    }

    override suspend fun setCompleted(habitId: Long, epochDay: Long, completed: Boolean) {
        if (completed) {
            completionDao.upsert(HabitCompletionEntity(habitId = habitId, epochDay = epochDay))
        } else {
            completionDao.delete(habitId = habitId, epochDay = epochDay)
        }
    }

    override suspend fun rescheduleAllReminders() {
        habitDao.getAllOnce()
            .map { it.toDomain() }
            .filter { it.reminderMinutesOfDay != null }
            .forEach { scheduler.schedule(it) }
    }

    private fun HabitEntity.toDomain() = Habit(
        id = id,
        name = name,
        iconCode = iconCode,
        daysMask = daysMask.coerceIn(1, Habit.ALL_DAYS_MASK),
        reminderMinutesOfDay = reminderMinutesOfDay?.coerceIn(0, Habit.MAX_MINUTE_OF_DAY),
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun Habit.toEntity(id: Long) = HabitEntity(
        id = id,
        name = name,
        iconCode = iconCode,
        daysMask = daysMask,
        reminderMinutesOfDay = reminderMinutesOfDay,
        createdAtEpochMillis = createdAtEpochMillis,
    )
}
