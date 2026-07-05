package com.runtimelabs.clarity.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Its own DataStore file, separate from [SettingsDataStore] — this
 * supersedes the `isPremiumUser` key that briefly lived there as a
 * placeholder (see ARCHITECTURE.md §24). A dedicated file keeps the
 * premium/billing subsystem self-contained rather than entangled with
 * unrelated app settings, and means a future data-migration concern for
 * one never touches the other.
 */
private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "clarity_premium",
)

@Singleton
class PremiumPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    private val safeData: Flow<Preferences> = context.premiumDataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }

    val isPremium: Flow<Boolean> = safeData
        .map { prefs -> prefs[Keys.IS_PREMIUM] ?: false }
        .distinctUntilChanged()

    suspend fun setIsPremium(value: Boolean) {
        context.premiumDataStore.edit { prefs -> prefs[Keys.IS_PREMIUM] = value }
    }
}
