package com.runtimelabs.clarity.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Registered in the manifest as the widget's provider. No Hilt injection
 * here: [ClarityWidget] is stateless per Google's own guidance (it's
 * recreated on every update) and reads its data via a plain DataStore
 * extension property, not constructor injection — see ClarityWidget.kt.
 */
class ClarityWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClarityWidget()
}
