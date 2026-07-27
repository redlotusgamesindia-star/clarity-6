package com.runtimelabs.clarity.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.runtimelabs.clarity.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Preferences DataStore for lightweight app settings.
 *
 * DataStore vs Room split (plan §5): key/value app configuration lives here;
 * anything that is *user data* (journey events, check-ins, journal) lives in
 * the encrypted Room DB. Settings are not sensitive; recovery data is.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "clarity_settings",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    // A corrupted-on-disk read should degrade to defaults, never crash the app.
    private val safeData: Flow<Preferences> = context.settingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }

    val themeMode: Flow<ThemeMode> = safeData
        .map { prefs -> ThemeMode.fromStorageValue(prefs[Keys.THEME_MODE]) }
        .distinctUntilChanged()

    val onboardingCompleted: Flow<Boolean> = safeData
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }
        .distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.storageValue }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[Keys.ONBOARDING_COMPLETED] = completed }
    }
}
