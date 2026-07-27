package com.runtimelabs.clarity.domain.model

/*
 * Every onboarding answer is a closed enum with a stable [storageValue],
 * decoupled from the enum name (same contract as ThemeMode): renames never
 * corrupt persisted rows, and the plan generator switches over exhaustive
 * types instead of strings.
 *
 * Display text deliberately does NOT live here — the domain layer is
 * locale-free; the UI maps each value to a string resource.
 */

/** Clarity is positioned as an adults-only app; ranges start at 18. */
enum class AgeRange(val storageValue: String) {
    AGE_18_24("18_24"),
    AGE_25_34("25_34"),
    AGE_35_44("35_44"),
    AGE_45_54("45_54"),
    AGE_55_PLUS("55_plus");

    companion object {
        fun fromStorageValue(value: String?): AgeRange =
            entries.firstOrNull { it.storageValue == value } ?: AGE_25_34
    }
}

enum class GenderIdentity(val storageValue: String) {
    MALE("male"),
    FEMALE("female"),
    NON_BINARY("non_binary"),
    PREFER_NOT_TO_SAY("prefer_not_to_say");

    companion object {
        fun fromStorageValue(value: String?): GenderIdentity =
            entries.firstOrNull { it.storageValue == value } ?: PREFER_NOT_TO_SAY
    }
}

enum class YearsAddicted(val storageValue: String) {
    LESS_THAN_ONE("lt_1"),
    ONE_TO_THREE("1_3"),
    THREE_TO_FIVE("3_5"),
    FIVE_TO_TEN("5_10"),
    OVER_TEN("gt_10");

    companion object {
        fun fromStorageValue(value: String?): YearsAddicted =
            entries.firstOrNull { it.storageValue == value } ?: ONE_TO_THREE
    }
}

enum class UsageFrequency(val storageValue: String) {
    MULTIPLE_DAILY("multiple_daily"),
    DAILY("daily"),
    SEVERAL_PER_WEEK("several_week"),
    WEEKLY("weekly"),
    FEW_PER_MONTH("few_month");

    companion object {
        fun fromStorageValue(value: String?): UsageFrequency =
            entries.firstOrNull { it.storageValue == value } ?: SEVERAL_PER_WEEK
    }
}

enum class MainTrigger(val storageValue: String) {
    STRESS("stress"),
    BOREDOM("boredom"),
    LONELINESS("loneliness"),
    FATIGUE("fatigue"),
    SOCIAL_MEDIA("social_media"),
    DIFFICULT_EMOTIONS("difficult_emotions");

    companion object {
        fun fromStorageValue(value: String?): MainTrigger =
            entries.firstOrNull { it.storageValue == value } ?: STRESS
    }
}

enum class RecoveryGoal(val storageValue: String) {
    QUIT_COMPLETELY("quit"),
    REBOOT_90_DAYS("reboot_90"),
    REDUCE_GRADUALLY("reduce");

    companion object {
        fun fromStorageValue(value: String?): RecoveryGoal =
            entries.firstOrNull { it.storageValue == value } ?: QUIT_COMPLETELY
    }
}

enum class ReasonToQuit(val storageValue: String) {
    MENTAL_CLARITY("mental_clarity"),
    RELATIONSHIPS("relationships"),
    SELF_RESPECT("self_respect"),
    ENERGY_PRODUCTIVITY("energy"),
    FAITH_VALUES("faith_values"),
    SEXUAL_HEALTH("sexual_health"),
    RECLAIM_TIME("reclaim_time");

    companion object {
        fun fromStorageValue(value: String?): ReasonToQuit? =
            entries.firstOrNull { it.storageValue == value }
    }
}

enum class PreviousStreak(val storageValue: String) {
    NEVER_TRIED("never"),
    DAYS_1_7("1_7"),
    WEEKS_1_4("8_30"),
    MONTHS_1_3("31_90"),
    OVER_90_DAYS("90_plus");

    companion object {
        fun fromStorageValue(value: String?): PreviousStreak =
            entries.firstOrNull { it.storageValue == value } ?: NEVER_TRIED
    }
}

enum class UrgeTime(val storageValue: String) {
    MORNING("morning"),
    AFTERNOON("afternoon"),
    EVENING("evening"),
    LATE_NIGHT("late_night"),
    UNPREDICTABLE("unpredictable");

    companion object {
        fun fromStorageValue(value: String?): UrgeTime =
            entries.firstOrNull { it.storageValue == value } ?: UNPREDICTABLE
    }
}

enum class SleepSchedule(val storageValue: String) {
    EARLY_CONSISTENT("early"),
    REGULAR("regular"),
    NIGHT_OWL("night_owl"),
    IRREGULAR("irregular");

    companion object {
        fun fromStorageValue(value: String?): SleepSchedule =
            entries.firstOrNull { it.storageValue == value } ?: REGULAR
    }
}
