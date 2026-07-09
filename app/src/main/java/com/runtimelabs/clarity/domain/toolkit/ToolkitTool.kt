package com.runtimelabs.clarity.domain.toolkit

/**
 * Every distinct thing the Emergency Toolkit offers, in one closed type —
 * the single vocabulary usage tracking, stats, and navigation all share, so
 * "most used tool" can never drift out of sync with what the toolkit
 * screen actually shows.
 */
enum class ToolkitTool(val storageValue: String) {
    BREATHING_OPEN("breathing_open"),
    BREATHING_30S("breathing_30s"),
    BREATHING_60S("breathing_60s"),
    BREATHING_2MIN("breathing_2min"),
    COLD_SHOWER("cold_shower"),
    WALK_OUTSIDE("walk_outside"),
    PUSH_UPS("push_ups"),
    DRINK_WATER("drink_water"),
    CALL_FRIEND("call_friend"),
    WRITE_JOURNAL("write_journal"),
    GROUNDING("grounding"),
    MUSCLE_RELAXATION("muscle_relaxation"),
    QUICK_REFRAME("quick_reframe"),
    MOTIVATION_WALL("motivation_wall"),
    DISTRACTION_IDEAS("distraction_ideas");

    companion object {
        fun fromStorageValue(value: String?): ToolkitTool? =
            entries.firstOrNull { it.storageValue == value }
    }
}
