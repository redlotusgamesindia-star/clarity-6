package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * App-level settings. Defined in the domain layer so ViewModels and use cases
 * depend on this interface, never on DataStore directly (dependency inversion).
 *
 * Grows in Phase A with: app-lock config, notification preferences,
 * discreet-mode flag.
 */
interface SettingsRepository {

    /** Reactive theme selection; emits immediately with the stored value. */
    val themeMode: Flow<ThemeMode>

    /** True once the user has finished (or skipped through) onboarding. */
    val onboardingCompleted: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setOnboardingCompleted(completed: Boolean)
}
