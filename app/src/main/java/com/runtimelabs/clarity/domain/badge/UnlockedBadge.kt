package com.runtimelabs.clarity.domain.badge

/**
 * One row of earned history. [unlockedAtEpochDay] drives display ("earned
 * on..."); [unlockedAtEpochMillis] is kept alongside it purely for stable
 * ordering, same split [com.runtimelabs.clarity.domain.toolkit.ToolkitUsageRecord]
 * already uses between a display-oriented day and an ordering-oriented
 * millis timestamp.
 */
data class UnlockedBadge(
    val badge: Badge,
    val unlockedAtEpochDay: Long,
    val unlockedAtEpochMillis: Long,
)
