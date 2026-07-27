package com.runtimelabs.clarity.domain.badge

/**
 * Lifetime achievement badges — a permanent collection, deliberately the
 * opposite shape from [com.runtimelabs.clarity.domain.recovery.ComebackAchievement],
 * which recomputes fresh per comeback with zero persisted "unlocked" state
 * (ARCHITECTURE.md §22) because it describes THIS comeback specifically.
 * Badges describe a lifetime of use: once earned, a badge stays earned even
 * after a later relapse resets the streak that unlocked it — this is
 * exactly the "lifetime-achievement model with a dedicated unlock-tracking
 * table" that [com.runtimelabs.clarity.domain.recovery.ComebackAchievement]'s
 * own doc comment names as the trade-off it isn't attempting to solve. A
 * dedicated `badge_unlock` table (see [com.runtimelabs.clarity.domain.repository.BadgeRepository])
 * is that table.
 *
 * [storageValue] is the persisted row id — same "never persist a bare enum
 * name" discipline as every other closed vocabulary in this app
 * ([com.runtimelabs.clarity.domain.model.JourneyEventType],
 * [com.runtimelabs.clarity.domain.model.RelapseTrigger],
 * [com.runtimelabs.clarity.domain.toolkit.ToolkitTool]): a future rename of
 * the Kotlin constant must never silently orphan someone's already-earned
 * badge.
 *
 * [category] is a structural fact about the badge (which section of the
 * collection it belongs to), not display text — same reasoning that keeps
 * [category] here rather than duplicated as a `when` in every UI file that
 * needs to group badges.
 */
enum class Badge(val storageValue: String, val category: BadgeCategory) {
    // Streak milestones — evaluated against the best streak ever reached
    // (StreakSnapshot.longestDays), not the current one. A badge already
    // earned must not need to be earned again just because a later relapse
    // brought the current streak back down; longestDays already carries
    // "best run ever, including the current one if it leads" (§22), which
    // is exactly the once-true-forever semantics a lifetime badge needs.
    DAY_1("day_1", BadgeCategory.STREAK),
    DAY_3("day_3", BadgeCategory.STREAK),
    DAY_7("day_7", BadgeCategory.STREAK),
    DAY_14("day_14", BadgeCategory.STREAK),
    DAY_21("day_21", BadgeCategory.STREAK),
    DAY_30("day_30", BadgeCategory.STREAK),
    DAY_50("day_50", BadgeCategory.STREAK),
    DAY_100("day_100", BadgeCategory.STREAK),
    DAY_365("day_365", BadgeCategory.STREAK),

    // Recovery milestones. Framed around relapses the same compassionate
    // way the Rebuild System already frames them (§22): a relapse isn't
    // just a setback in this app's own vocabulary, it's the moment a new
    // recovery run begins. Reaching for help and starting again is the
    // thing being celebrated, not the slip itself.
    FIRST_RECOVERY("first_recovery", BadgeCategory.RECOVERY),
    FIVE_RECOVERIES("five_recoveries", BadgeCategory.RECOVERY),

    // Daily-practice engagement — the habits this app is built to encourage
    // outside of raw days-clean: checking in early, writing, and coming
    // back to the coping-skills toolkit on consecutive days.
    MORNING_CHECK_IN("morning_check_in", BadgeCategory.DAILY_PRACTICE),
    JOURNAL_WRITER("journal_writer", BadgeCategory.DAILY_PRACTICE),
    LEARNING_STREAK("learning_streak", BadgeCategory.DAILY_PRACTICE),
    ;

    companion object {
        fun fromStorageValue(value: String?): Badge? =
            entries.firstOrNull { it.storageValue == value }

        /** Streak badges only, in ascending threshold order — the app's canonical day-count ladder. */
        val STREAK_LADDER: List<Badge> = listOf(DAY_1, DAY_3, DAY_7, DAY_14, DAY_21, DAY_30, DAY_50, DAY_100, DAY_365)
    }
}

enum class BadgeCategory {
    STREAK,
    RECOVERY,
    DAILY_PRACTICE,
}
