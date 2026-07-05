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

internal val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
