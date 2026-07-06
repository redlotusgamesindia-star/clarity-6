package com.runtimelabs.clarity.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.runtimelabs.clarity.MainActivity
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.data.local.datastore.WIDGET_SNAPSHOT_DATASTORE_NAME
import com.runtimelabs.clarity.domain.model.WidgetSnapshot
import kotlinx.coroutines.flow.first

// Same file name as data/local/datastore/WidgetSnapshotStore.kt's delegate —
// that is what makes the two resolve to the same on-disk cache. See that
// file's doc comment for why this is safe without sharing a Kotlin property.
private val Context.widgetSnapshotDataStore by preferencesDataStore(name = WIDGET_SNAPSHOT_DATASTORE_NAME)

private object WidgetKeys {
    val CURRENT_DAYS = intPreferencesKey("current_days")
    val MILESTONE_DAYS = intPreferencesKey("milestone_days")
}

private val SMALL_SIZE = DpSize(110.dp, 110.dp)
private val LARGE_SIZE = DpSize(250.dp, 150.dp)

/**
 * The streak widget. Stateless and passive by design (Glance recreates this
 * class on every update) — all data comes from the DataStore cache that
 * [com.runtimelabs.clarity.domain.repository.WidgetSyncRepository] keeps
 * fresh; this class never touches Room, Hilt, or the encrypted database.
 *
 * Two fixed sizes via [SizeMode.Responsive] rather than [SizeMode.Exact]:
 * the layouts below are hand-designed for exactly these two footprints, not
 * arbitrary ones, which keeps the Glance surface (already the least
 * compiler-checked part of this codebase) as simple as it can be.
 */
class ClarityWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL_SIZE, LARGE_SIZE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.widgetSnapshotDataStore.data.first()
        val snapshot = WidgetSnapshot(
            currentDays = prefs[WidgetKeys.CURRENT_DAYS] ?: 0,
            milestoneDays = prefs[WidgetKeys.MILESTONE_DAYS] ?: DEFAULT_MILESTONE_DAYS,
        )
        val daysCleanLabel = context.getString(R.string.widget_days_clean_label)
        val milestoneCaption = context.getString(
            R.string.widget_milestone_caption,
            snapshot.currentDays,
            snapshot.milestoneDays,
        )

        // Built here, not inside ClarityWidgetContent: actionStartActivity in
        // this Glance version takes an Intent directly (confirmed against
        // the compiler's own overload-resolution error, not guessed), and
        // context is already in scope in provideGlance. FLAG_ACTIVITY_NEW_TASK
        // is required since this launches from outside any Activity context
        // (a widget tap, dispatched by the launcher process).
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        provideContent {
            val isSmall = LocalSize.current.width < LARGE_SIZE.width
            ClarityWidgetContent(
                snapshot = snapshot,
                isSmall = isSmall,
                daysCleanLabel = daysCleanLabel,
                milestoneCaption = milestoneCaption,
                launchIntent = launchIntent,
            )
        }
    }

    private companion object {
        const val DEFAULT_MILESTONE_DAYS = 7
    }
}
