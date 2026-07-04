package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.datastore.SettingsDataStore
import com.runtimelabs.clarity.domain.model.ThemeMode
import com.runtimelabs.clarity.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Thin today by design: when settings gain validation or migration logic
 * (Phase A adds app-lock and notification prefs), it lands here without
 * touching any ViewModel.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = dataStore.themeMode

    override val onboardingCompleted: Flow<Boolean> = dataStore.onboardingCompleted

    override suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        dataStore.setOnboardingCompleted(completed)
}
