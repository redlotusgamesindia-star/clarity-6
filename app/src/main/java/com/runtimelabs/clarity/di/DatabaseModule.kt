package com.runtimelabs.clarity.di

import android.content.Context
import androidx.room.Room
import com.runtimelabs.clarity.core.security.DatabasePassphraseManager
import com.runtimelabs.clarity.data.local.db.ALL_MIGRATIONS
import com.runtimelabs.clarity.data.local.db.ClarityDatabase
import com.runtimelabs.clarity.data.local.db.dao.AppMetadataDao
import com.runtimelabs.clarity.data.local.db.dao.BadgeUnlockDao
import com.runtimelabs.clarity.data.local.db.dao.CheckInDao
import com.runtimelabs.clarity.data.local.db.dao.GratitudeDao
import com.runtimelabs.clarity.data.local.db.dao.HabitCompletionDao
import com.runtimelabs.clarity.data.local.db.dao.HabitDao
import com.runtimelabs.clarity.data.local.db.dao.JournalDao
import com.runtimelabs.clarity.data.local.db.dao.JourneyDao
import com.runtimelabs.clarity.data.local.db.dao.RelapseReflectionDao
import com.runtimelabs.clarity.data.local.db.dao.ThoughtRecordDao
import com.runtimelabs.clarity.data.local.db.dao.ToolkitUsageDao
import com.runtimelabs.clarity.data.local.db.dao.RecoveryProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClarityDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager,
    ): ClarityDatabase {
        // Load the SQLCipher native library before any factory use.
        // Safe to call repeatedly; the runtime ignores duplicate loads.
        System.loadLibrary("sqlcipher")

        val factory = SupportOpenHelperFactory(passphraseManager.getOrCreatePassphrase())

        return Room.databaseBuilder(context, ClarityDatabase::class.java, ClarityDatabase.DATABASE_NAME)
            .openHelperFactory(factory)
            .addMigrations(*ALL_MIGRATIONS)
            // No fallbackToDestructiveMigration — this database will hold a
            // user's entire recovery history; destroying it is never an
            // acceptable failure mode. Real migrations from v2 onward.
            .build()
    }

    @Provides
    fun provideAppMetadataDao(database: ClarityDatabase): AppMetadataDao =
        database.appMetadataDao()

    @Provides
    fun provideRecoveryProfileDao(database: ClarityDatabase): RecoveryProfileDao =
        database.recoveryProfileDao()

    @Provides
    fun provideCheckInDao(database: ClarityDatabase): CheckInDao =
        database.checkInDao()

    @Provides
    fun provideJournalDao(database: ClarityDatabase): JournalDao =
        database.journalDao()

    @Provides
    fun provideJourneyDao(database: ClarityDatabase): JourneyDao =
        database.journeyDao()

    @Provides
    fun provideHabitDao(database: ClarityDatabase): HabitDao =
        database.habitDao()

    @Provides
    fun provideHabitCompletionDao(database: ClarityDatabase): HabitCompletionDao =
        database.habitCompletionDao()

    @Provides
    fun provideThoughtRecordDao(database: ClarityDatabase): ThoughtRecordDao =
        database.thoughtRecordDao()

    @Provides
    fun provideGratitudeDao(database: ClarityDatabase): GratitudeDao =
        database.gratitudeDao()

    @Provides
    fun provideRelapseReflectionDao(database: ClarityDatabase): RelapseReflectionDao =
        database.relapseReflectionDao()

    // Genuinely missing before this pass — ToolkitUsageRepositoryImpl has
    // taken a ToolkitUsageDao constructor dependency since the toolkit-
    // expansion pass (§29), but nothing ever provided it here. Found by
    // inspection while wiring the badge system (which also reads toolkit
    // usage for the Learning Streak badge), not assumed fixed. Flagged
    // explicitly per ARCHITECTURE.md §26's own lesson: grep before
    // assuming an existing wire is connected.
    @Provides
    fun provideToolkitUsageDao(database: ClarityDatabase): ToolkitUsageDao =
        database.toolkitUsageDao()

    @Provides
    fun provideBadgeUnlockDao(database: ClarityDatabase): BadgeUnlockDao =
        database.badgeUnlockDao()
}
