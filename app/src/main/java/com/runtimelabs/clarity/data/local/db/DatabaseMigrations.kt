package com.runtimelabs.clarity.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Hand-written migrations, one per version bump, per the no-destructive-
 * migration rule (ARCHITECTURE.md §6). SQL is written to match Room's
 * generated schema exactly (verify against app/schemas/2.json after the
 * first build).
 */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recovery_profile` (" +
                "`id` INTEGER NOT NULL, " +
                "`ageRange` TEXT NOT NULL, " +
                "`gender` TEXT, " +
                "`yearsAddicted` TEXT NOT NULL, " +
                "`frequency` TEXT NOT NULL, " +
                "`mainTrigger` TEXT NOT NULL, " +
                "`goal` TEXT NOT NULL, " +
                "`motivationLevel` INTEGER NOT NULL, " +
                "`reasonsToQuit` TEXT NOT NULL, " +
                "`previousStreak` TEXT NOT NULL, " +
                "`strongestUrgeTime` TEXT NOT NULL, " +
                "`sleepSchedule` TEXT NOT NULL, " +
                "`firstMilestoneDays` INTEGER NOT NULL, " +
                "`focusAreas` TEXT NOT NULL, " +
                "`createdAtEpochMillis` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recovery_plan_item` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`code` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`orderIndex` INTEGER NOT NULL, " +
                "`isCompleted` INTEGER NOT NULL)",
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `daily_checkin` (" +
                "`epochDay` INTEGER NOT NULL, " +
                "`mood` INTEGER NOT NULL, " +
                "`urgeLevel` INTEGER NOT NULL, " +
                "`updatedAtEpochMillis` INTEGER NOT NULL, " +
                "PRIMARY KEY(`epochDay`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `journal_entry` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`createdAtEpochMillis` INTEGER NOT NULL, " +
                "`updatedAtEpochMillis` INTEGER NOT NULL, " +
                "`body` TEXT NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `journey_event` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`occurredAtEpochMillis` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL)",
        )
    }
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `habit` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`iconCode` TEXT NOT NULL, " +
                "`daysMask` INTEGER NOT NULL, " +
                "`reminderMinutesOfDay` INTEGER, " +
                "`createdAtEpochMillis` INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `habit_completion` (" +
                "`habitId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "PRIMARY KEY(`habitId`, `epochDay`))",
        )
    }
}

internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `thought_record` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`createdAtEpochMillis` INTEGER NOT NULL, " +
                "`updatedAtEpochMillis` INTEGER NOT NULL, " +
                "`situation` TEXT NOT NULL, " +
                "`automaticThought` TEXT NOT NULL, " +
                "`feeling` TEXT NOT NULL, " +
                "`feelingIntensity` INTEGER NOT NULL, " +
                "`reframe` TEXT NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `gratitude_entry` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`createdAtEpochMillis` INTEGER NOT NULL, " +
                "`updatedAtEpochMillis` INTEGER NOT NULL, " +
                "`first` TEXT NOT NULL, " +
                "`second` TEXT, " +
                "`third` TEXT)",
        )
    }
}

internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `relapse_reflection` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`journeyEventId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`createdAtEpochMillis` INTEGER NOT NULL, " +
                "`trigger` TEXT, " +
                "`timeOfDay` TEXT, " +
                "`mood` INTEGER, " +
                "`location` TEXT, " +
                "`notes` TEXT NOT NULL)",
        )
    }
}

internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // The recovery flow's reflection step was redesigned around a new,
        // more specific vocabulary (setbackType/emotion/trigger replacing
        // trigger/timeOfDay/mood/location/notes) — a clean rebuild of this
        // one table rather than an in-place column migration, since the
        // old free-text notes and location fields have no equivalent in
        // the new shape and there is no way to meaningfully carry old rows
        // forward. This table only ever added optional color to the
        // already-durable journey_event record of a relapse (§22) — no
        // relapse history is lost by this, only the optional reflection
        // notes some past relapses may have had attached.
        db.execSQL("DROP TABLE IF EXISTS `relapse_reflection`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `relapse_reflection` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`journeyEventId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`createdAtEpochMillis` INTEGER NOT NULL, " +
                "`setbackType` TEXT, " +
                "`emotion` TEXT, " +
                "`trigger` TEXT)",
        )
    }
}

internal val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `toolkit_usage` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`tool` TEXT NOT NULL, " +
                "`startedAtEpochMillis` INTEGER NOT NULL, " +
                "`durationSeconds` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL)",
        )
    }
}

internal val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `badge_unlock` (" +
                "`badge` TEXT NOT NULL, " +
                "`unlockedAtEpochDay` INTEGER NOT NULL, " +
                "`unlockedAtEpochMillis` INTEGER NOT NULL, " +
                "PRIMARY KEY(`badge`))",
        )
    }
}

internal val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
    MIGRATION_8_9,
)
