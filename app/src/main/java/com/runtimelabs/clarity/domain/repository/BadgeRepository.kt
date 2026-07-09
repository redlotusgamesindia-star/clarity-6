package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.badge.UnlockedBadge
import kotlinx.coroutines.flow.Flow

/**
 * Orchestrates the badge collection the same way
 * [com.runtimelabs.clarity.domain.repository.WidgetSyncRepository] orchestrates
 * the widget cache: one coordinating action ([evaluateAndUnlock]) that reads
 * across several other repositories plus a pure calculator
 * ([com.runtimelabs.clarity.domain.badge.BadgeEvaluator]), called from
 * several sites rather than reinvented at each one (ARCHITECTURE.md §20's
 * "one method, four call sites" idiom).
 */
interface BadgeRepository {

    /** Every badge ever earned, oldest first. Drives the Achievements screen and its Journey preview. */
    fun observeUnlocked(): Flow<List<UnlockedBadge>>

    /**
     * Re-checks every badge against current app state and persists any
     * newly-qualified ones. Safe to call as often as convenient — already-
     * unlocked badges are never re-evaluated as "new" (insert is conflict-
     * ignored at the DAO, and the already-unlocked set is filtered before
     * that). Returns only the badges genuinely unlocked BY THIS CALL, so a
     * caller can tell a real just-now unlock from a badge that turned out
     * to already be earned — the signal the unlock-celebration UI needs.
     */
    suspend fun evaluateAndUnlock(): List<Badge>
}
