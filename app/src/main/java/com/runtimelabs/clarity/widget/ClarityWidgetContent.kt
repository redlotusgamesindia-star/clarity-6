package com.runtimelabs.clarity.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.runtimelabs.clarity.domain.model.WidgetSnapshot

/*
 * Glance content only. Deliberately no androidx.compose.foundation or
 * androidx.compose.material3 imports anywhere in this file — Glance renders
 * through RemoteViews and has its own layout/text vocabulary (GlanceModifier,
 * not Modifier; androidx.glance.text.Text, not the Compose one). Base value
 * types (Dp, sp) ARE shared with mainstream Compose and are fine here; see
 * ARCHITECTURE.md §20 for the full explanation of what is and isn't safe to
 * import in this package.
 *
 * There is no determinate circular progress composable in Glance 1.1.0 (only
 * an indeterminate spinner) — RemoteViews' Canvas-free drawing model can't
 * support the app's Home-screen ring. The large size below uses a horizontal
 * LinearProgressIndicator instead: a deliberate, documented adaptation to
 * the platform's real constraints, not an oversight.
 */

@Composable
internal fun ClarityWidgetContent(
    snapshot: WidgetSnapshot,
    isSmall: Boolean,
    daysCleanLabel: String,
    milestoneCaption: String,
    launchIntent: Intent,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(launchIntent))
            .background(WidgetColors.background)
            .appWidgetBackground()
            .padding(if (isSmall) 12.dp else 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSmall) {
            SmallContent(snapshot, daysCleanLabel)
        } else {
            LargeContent(snapshot, daysCleanLabel, milestoneCaption)
        }
    }
}

@Composable
private fun SmallContent(snapshot: WidgetSnapshot, daysCleanLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = snapshot.currentDays.toString(),
            style = TextStyle(color = WidgetColors.primary, fontSize = 32.sp, fontWeight = FontWeight.Bold),
        )
        Text(
            text = daysCleanLabel,
            style = TextStyle(color = WidgetColors.onSurfaceVariant, fontSize = 12.sp),
        )
    }
}

@Composable
private fun LargeContent(
    snapshot: WidgetSnapshot,
    daysCleanLabel: String,
    milestoneCaption: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.fillMaxWidth(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = snapshot.currentDays.toString(),
                style = TextStyle(color = WidgetColors.primary, fontSize = 40.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = daysCleanLabel,
                style = TextStyle(color = WidgetColors.onSurfaceVariant, fontSize = 12.sp),
            )
        }
        Spacer(modifier = GlanceModifier.width(20.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = milestoneCaption,
                style = TextStyle(color = WidgetColors.onSurfaceVariant, fontSize = 13.sp),
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            LinearProgressIndicator(
                progress = snapshot.progressFraction,
                modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                color = if (snapshot.milestoneReached) WidgetColors.celebration else WidgetColors.primary,
                backgroundColor = WidgetColors.track,
            )
        }
    }
}
