package com.runtimelabs.clarity.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache the app WRITES to for the home-screen widget to read.
 *
 * Deliberately unencrypted, unlike every other user-data store in this app
 * (plan §5, §14). This is not a privacy regression: the two numbers here
 * (days clean, milestone target) are the exact numbers the widget renders
 * in cleartext on the home screen the moment the user places it — encrypting
 * the on-disk cache of a value already displayed in the open would protect
 * nothing. Everything with actual content (journal text, thought records,
 * feelings, triggers) stays exclusively in the encrypted Room database and
 * never reaches this file. See ARCHITECTURE.md §20.
 *
 * The widget itself (a different composition context, not part of the Hilt
 * graph) reads the SAME on-disk file via its own identically-named
 * `preferencesDataStore` delegate in [com.runtimelabs.clarity.widget.ClarityWidget]
 * — DataStore coordinates multiple such delegates safely as long as the
 * `name` matches exactly; there is no need to share one Kotlin property
 * across the two call sites.
 */
private val Context.widgetSnapshotDataStore: DataStore<Preferences> by preferencesDataStore(
    name = WIDGET_SNAPSHOT_DATASTORE_NAME,
)

internal const val WIDGET_SNAPSHOT_DATASTORE_NAME = "clarity_widget_snapshot"

@Singleton
class WidgetSnapshotStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val CURRENT_DAYS = intPreferencesKey("current_days")
        val MILESTONE_DAYS = intPreferencesKey("milestone_days")
    }

    suspend fun write(currentDays: Int, milestoneDays: Int) {
        context.widgetSnapshotDataStore.edit { prefs ->
            prefs[Keys.CURRENT_DAYS] = currentDays
            prefs[Keys.MILESTONE_DAYS] = milestoneDays
        }
    }
}
