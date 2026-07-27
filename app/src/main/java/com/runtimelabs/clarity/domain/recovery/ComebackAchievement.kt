package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.StreakSnapshot

/**
 * Five badges tied to THIS comeback specifically, not a lifetime collection.
 * Deliberately derived live from [StreakSnapshot] with zero persisted
 * "unlocked" state — same "derive, don't store" discipline as the streak
 * itself (AD-1), which sidesteps an entire class of sync bugs (no risk of a
 * badge disagreeing with the data it's supposedly summarizing). The
 * trade-off, stated plainly: if a person relapses again, these recompute
 * fresh for the new comeback rather than staying permanently lit from the
 * last one — treated as a feature (each comeback earns its own pride) not a
 * limitation, and it avoids real complexity a lifetime-achievement model
 * would need with no dedicated unlock-tracking table to match it against.
 */
enum class ComebackAchievement {
    STARTED_AGAIN,
    DIDNT_QUIT,
    FIRST_WEEK_BACK,
    STRONGER_THAN_BEFORE,
    BEAT_PREVIOUS_RECORD,
}

fun StreakSnapshot.unlockedComebackAchievements(): List<ComebackAchievement> {
    if (totalRelapses <= 0) return emptyList()
    val unlocked = mutableListOf<ComebackAchievement>()
    unlocked += ComebackAchievement.STARTED_AGAIN
    if (currentDays >= 3) unlocked += ComebackAchievement.DIDNT_QUIT
    if (currentDays >= 7) unlocked += ComebackAchievement.FIRST_WEEK_BACK
    val previous = previousRunDays
    if (previous != null && currentDays >= maxOf(previous / 2, 7)) {
        unlocked += ComebackAchievement.STRONGER_THAN_BEFORE
    }
    if (hasBeatenPreviousRecord) unlocked += ComebackAchievement.BEAT_PREVIOUS_RECORD
    return unlocked
}
