package com.runtimelabs.clarity.domain.model

/**
 * Deliberately its own vocabulary, not the onboarding/insight system's
 * [MainTrigger] + [UrgeTime] split — NIGHT and COULDNT_SLEEP are genuine,
 * nameable triggers in their own right here, not just a time-of-day
 * modifier on some other trigger. Folding "when" into "what triggered it"
 * as one flat list matches how this specific question was asked.
 */
enum class RelapseTrigger(val storageValue: String) {
    STRESS("stress"),
    LONELINESS("loneliness"),
    SOCIAL_MEDIA("social_media"),
    BOREDOM("boredom"),
    NIGHT("night"),
    COULDNT_SLEEP("couldnt_sleep"),
    OTHER("other");

    companion object {
        fun fromStorageValue(value: String?): RelapseTrigger? =
            entries.firstOrNull { it.storageValue == value }
    }
}
