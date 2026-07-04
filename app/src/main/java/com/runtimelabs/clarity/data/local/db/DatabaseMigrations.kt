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

internal val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
