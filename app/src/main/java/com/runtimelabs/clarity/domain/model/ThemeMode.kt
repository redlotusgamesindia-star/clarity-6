package com.runtimelabs.clarity.domain.model

/**
 * User-selectable theme. SYSTEM is the default: the calm/dark aesthetic is a
 * suggestion, never an imposition.
 *
 * [storageValue] is a stable string key decoupled from the enum name so a
 * future rename never corrupts persisted preferences.
 */
enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorageValue(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}
