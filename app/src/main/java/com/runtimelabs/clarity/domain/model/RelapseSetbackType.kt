package com.runtimelabs.clarity.domain.model

/** What happened, in the person's own terms — the first, most concrete question in the recovery flow. */
enum class RelapseSetbackType(val storageValue: String) {
    PORN("porn"),
    MASTURBATION("masturbation"),
    BOTH("both"),
    URGE_ONLY("urge_only");

    companion object {
        fun fromStorageValue(value: String?): RelapseSetbackType? =
            entries.firstOrNull { it.storageValue == value }
    }
}
