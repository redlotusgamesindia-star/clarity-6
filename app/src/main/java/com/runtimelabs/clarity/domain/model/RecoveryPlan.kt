package com.runtimelabs.clarity.domain.model

/**
 * Plan sections, in display order. FOUNDATION always comes first; the rest
 * appear only when the generator has something personalized to put there.
 */
enum class PlanCategory(val storageValue: String, val displayRank: Int) {
    FOUNDATION("foundation", 0),
    TRIGGER_DEFENSE("trigger_defense", 1),
    ENVIRONMENT("environment", 2),
    SLEEP("sleep", 3),
    MINDSET("mindset", 4);

    companion object {
        fun fromStorageValue(value: String?): PlanCategory =
            entries.firstOrNull { it.storageValue == value } ?: FOUNDATION
    }
}

/**
 * Plan items are stable *codes*, not prose. The UI resolves each code to
 * localized title/description strings; the database stores only the code.
 * Copy improvements and translations ship with app updates without touching
 * anyone's saved plan.
 */
enum class PlanItemCode(val storageValue: String) {
    // Foundation
    DAILY_CHECKIN("daily_checkin"),
    LEARN_URGE_TOOLKIT("learn_urge_toolkit"),
    DEFINE_YOUR_WHY("define_your_why"),
    FIRST_MILESTONE_FOCUS("first_milestone_focus"),

    // Goal framing
    ABSTINENCE_IDENTITY("abstinence_identity"),
    REBOOT_90_FRAME("reboot_90_frame"),
    REDUCTION_LADDER("reduction_ladder"),

    // Frequency severity
    HIGH_RISK_MOMENT_MAP("high_risk_moment_map"),
    DEVICE_FRICTION_SETUP("device_friction_setup"),

    // Trigger defenses
    STRESS_RESET_BREATHING("stress_reset_breathing"),
    BOREDOM_REPLACEMENT_LIST("boredom_replacement_list"),
    CONNECTION_PLAN("connection_plan"),
    FATIGUE_ENERGY_AUDIT("fatigue_energy_audit"),
    SOCIAL_MEDIA_HYGIENE("social_media_hygiene"),
    EMOTION_JOURNALING("emotion_journaling"),
    IF_THEN_PLANS("if_then_plans"),

    // Urge-time routines
    LATE_NIGHT_DEVICE_CURFEW("late_night_device_curfew"),
    MORNING_LAUNCH_ROUTINE("morning_launch_routine"),
    EVENING_TRANSITION_RITUAL("evening_transition_ritual"),
    AFTERNOON_RESET_WALK("afternoon_reset_walk"),

    // Sleep
    WIND_DOWN_ROUTINE("wind_down_routine"),
    SLEEP_ANCHOR_TIME("sleep_anchor_time"),

    // Mindset / motivation calibration
    START_TINY_COMMITMENT("start_tiny_commitment"),
    CHANNEL_MOMENTUM("channel_momentum"),
    STREAK_PATTERN_REVIEW("streak_pattern_review");

    companion object {
        fun fromStorageValue(value: String?): PlanItemCode? =
            entries.firstOrNull { it.storageValue == value }
    }
}

data class RecoveryPlanItem(
    val code: PlanItemCode,
    val category: PlanCategory,
    val orderIndex: Int,
    val isCompleted: Boolean = false, // becomes actionable in the tasks feature
)

data class RecoveryPlan(
    /** First streak milestone to aim for, personalized from history. */
    val firstMilestoneDays: Int,
    /** Up to three personalized emphasis areas (never FOUNDATION). */
    val focusAreas: List<PlanCategory>,
    val items: List<RecoveryPlanItem>,
)
