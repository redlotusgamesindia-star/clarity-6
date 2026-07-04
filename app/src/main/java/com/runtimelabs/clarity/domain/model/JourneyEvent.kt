package com.runtimelabs.clarity.domain.model

/**
 * Append-only recovery timeline (AD-1: event sourcing). Facts are recorded,
 * never edited; everything else — streaks, milestones, insights — is derived.
 * Only RELAPSE exists today; future kinds (milestone celebrations, urge
 * survivals) are new rows, not schema changes.
 */
enum class JourneyEventType(val storageValue: String) {
    RELAPSE("relapse");

    companion object {
        fun fromStorageValue(value: String?): JourneyEventType? =
            entries.firstOrNull { it.storageValue == value }
    }
}

data class JourneyEvent(
    val id: Long,
    val type: JourneyEventType,
    val occurredAtEpochMillis: Long,
    /** Local calendar day the event counts against (timezone decided at write time). */
    val epochDay: Long,
)
