package com.runtimelabs.clarity.domain.recovery

/**
 * Stable codes, same i18n contract as [com.runtimelabs.clarity.domain.model.PlanItemCode]
 * — the checklist step never stores or generates prose directly.
 */
enum class RecoveryChecklistItemCode {
    DRINK_WATER,
    TAKE_WALK,
    SHOWER,
    JOURNAL_IT,
    BREATHING_EXERCISE,
    REACH_OUT,
    PLAN_NEXT_HOUR,
    WIND_DOWN,
}

data class RecoveryChecklistItem(val code: RecoveryChecklistItemCode)
