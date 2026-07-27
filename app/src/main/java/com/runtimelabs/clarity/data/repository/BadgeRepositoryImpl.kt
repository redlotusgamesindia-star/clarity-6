package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.BadgeUnlockDao
import com.runtimelabs.clarity.data.local.db.entity.BadgeUnlockEntity
import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.badge.BadgeEvaluator
import com.runtimelabs.clarity.domain.badge.BadgeStats
import com.runtimelabs.clarity.domain.badge.UnlockedBadge
import com.runtimelabs.clarity.domain.badge.longestConsecutiveRun
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.repository.BadgeRepository
import com.runtimelabs.clarity.domain.repository.CheckInRepository
import com.runtimelabs.clarity.domain.repository.JournalRepository
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.streak.StreakCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * The one place that turns "everything else this app already tracks" into
 * "which badges does that qualify for" — same orchestrator shape as
 * [WidgetSyncRepositoryImpl]: reads across several repositories plus a
 * calculator, called from several sites (ARCHITECTURE.md §20's "one
 * method, four call sites" idiom), rather than that composition being
 * reinvented at each call site.
 *
 * All timezone-aware conversion happens here, never inside [BadgeEvaluator]
 * — same split [StreakCalculator]'s callers already use ("today is a
 * parameter, never a clock read").
 */
@Singleton
class BadgeRepositoryImpl @Inject constructor(
    private val badgeUnlockDao: BadgeUnlockDao,
    private val profileRepository: RecoveryProfileRepository,
    private val journeyRepository: JourneyRepository,
    private val checkInRepository: CheckInRepository,
    private val journalRepository: JournalRepository,
    private val toolkitUsageRepository: ToolkitUsageRepository,
    private val streakCalculator: StreakCalculator,
    private val badgeEvaluator: BadgeEvaluator,
) : BadgeRepository {

    override fun observeUnlocked(): Flow<List<UnlockedBadge>> =
        badgeUnlockDao.observeAll().map { entities ->
            entities.mapNotNull { entity ->
                val badge = Badge.fromStorageValue(entity.badge) ?: return@mapNotNull null
                UnlockedBadge(
                    badge = badge,
                    unlockedAtEpochDay = entity.unlockedAtEpochDay,
                    unlockedAtEpochMillis = entity.unlockedAtEpochMillis,
                )
            }
        }

    override suspend fun evaluateAndUnlock(): List<Badge> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now().toEpochDay()

        val profile = profileRepository.profile.first()
        val startDay = profile?.createdAtEpochMillis
            ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate().toEpochDay() }
            ?: today
        val relapseDays = journeyRepository.observeEventDays(JourneyEventType.RELAPSE).first()
        val streak = streakCalculator.compute(
            recoveryStartEpochDay = startDay,
            relapseEpochDays = relapseDays,
            todayEpochDay = today,
        )

        // observeSince(0) reads every check-in ever recorded — 0 is safely
        // before any real epoch day, same "since the beginning" sentinel
        // use as passing a start-of-time bound anywhere else in this app.
        val checkIns = checkInRepository.observeSince(0L).first()
        val hasMorningCheckIn = checkIns.any { checkIn ->
            Instant.ofEpochMilli(checkIn.updatedAtEpochMillis).atZone(zone).hour < MORNING_CUTOFF_HOUR
        }

        val journalEntryCount = journalRepository.observeAll().first().size

        val toolkitDays = toolkitUsageRepository.observeAll().first().map { it.epochDay }
        val longestToolkitStreak = longestConsecutiveRun(toolkitDays)

        val stats = BadgeStats(
            longestStreakDays = streak.longestDays,
            totalRelapses = streak.totalRelapses,
            hasMorningCheckIn = hasMorningCheckIn,
            journalEntryCount = journalEntryCount,
            longestToolkitUsageStreakDays = longestToolkitStreak,
        )

        val alreadyUnlocked = badgeUnlockDao.getUnlockedCodes()
            .mapNotNull { Badge.fromStorageValue(it) }
            .toSet()

        val newlyQualified = badgeEvaluator.evaluate(stats, alreadyUnlocked)
        if (newlyQualified.isEmpty()) return emptyList()

        val nowMillis = System.currentTimeMillis()
        val genuinelyNew = mutableListOf<Badge>()
        newlyQualified.forEach { badge ->
            val rowId = badgeUnlockDao.insert(
                BadgeUnlockEntity(
                    badge = badge.storageValue,
                    unlockedAtEpochDay = today,
                    unlockedAtEpochMillis = nowMillis,
                ),
            )
            // -1 means the conflict strategy ignored the insert because a
            // concurrent call already wrote this badge first — not a new
            // unlock from THIS call, so it's excluded from the result the
            // celebration UI acts on (see BadgeUnlockDao.insert's doc).
            if (rowId != -1L) genuinelyNew += badge
        }
        return genuinelyNew
    }

    private companion object {
        /** Same "before noon" boundary Home's own greeting already uses for "morning". */
        const val MORNING_CUTOFF_HOUR = 12
    }
}
