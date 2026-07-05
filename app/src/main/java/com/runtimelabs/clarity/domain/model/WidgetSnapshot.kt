package com.runtimelabs.clarity.domain.model

/**
 * The minimal, denormalized display state the home-screen widget renders.
 * Deliberately just two numbers — no mood, no journal, no triggers. See
 * ARCHITECTURE.md §20 for why caching this outside the encrypted DB is an
 * honest choice, not a privacy regression.
 */
data class WidgetSnapshot(
    val currentDays: Int,
    val milestoneDays: Int,
) {
    val progressFraction: Float
        get() = if (milestoneDays <= 0) 0f else (currentDays / milestoneDays.toFloat()).coerceIn(0f, 1f)

    val milestoneReached: Boolean
        get() = milestoneDays > 0 && currentDays >= milestoneDays
}
