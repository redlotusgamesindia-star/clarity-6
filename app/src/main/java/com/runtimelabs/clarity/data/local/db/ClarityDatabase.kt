package com.runtimelabs.clarity.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.runtimelabs.clarity.data.local.db.dao.AppMetadataDao
import com.runtimelabs.clarity.data.local.db.dao.CheckInDao
import com.runtimelabs.clarity.data.local.db.dao.HabitCompletionDao
import com.runtimelabs.clarity.data.local.db.dao.HabitDao
import com.runtimelabs.clarity.data.local.db.dao.JournalDao
import com.runtimelabs.clarity.data.local.db.dao.JourneyDao
import com.runtimelabs.clarity.data.local.db.dao.RecoveryProfileDao
import com.runtimelabs.clarity.data.local.db.entity.AppMetadataEntity
import com.runtimelabs.clarity.data.local.db.entity.DailyCheckInEntity
import com.runtimelabs.clarity.data.local.db.entity.HabitCompletionEntity
import com.runtimelabs.clarity.data.local.db.entity.HabitEntity
import com.runtimelabs.clarity.data.local.db.entity.JournalEntryEntity
import com.runtimelabs.clarity.data.local.db.entity.JourneyEventEntity
import com.runtimelabs.clarity.data.local.db.entity.RecoveryPlanItemEntity
import com.runtimelabs.clarity.data.local.db.entity.RecoveryProfileEntity

/**
 * The app database. Encrypted end-to-end with SQLCipher — the factory is
 * wired in [com.runtimelabs.clarity.di.DatabaseModule], so no code below
 * the DI layer ever knows encryption exists.
 *
 * exportSchema = true + the ksp schemaLocation arg means every version's
 * schema JSON is committed under /app/schemas, which is what makes
 * migration tests possible from v2 onward.
 */
@Database(
    entities = [
        AppMetadataEntity::class,
        RecoveryProfileEntity::class,
        RecoveryPlanItemEntity::class,
        DailyCheckInEntity::class,
        JournalEntryEntity::class,
        JourneyEventEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class ClarityDatabase : RoomDatabase() {

    abstract fun appMetadataDao(): AppMetadataDao

    abstract fun recoveryProfileDao(): RecoveryProfileDao

    abstract fun checkInDao(): CheckInDao

    abstract fun journalDao(): JournalDao

    abstract fun journeyDao(): JourneyDao

    abstract fun habitDao(): HabitDao

    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {
        const val DATABASE_NAME = "clarity.db"
    }
}
