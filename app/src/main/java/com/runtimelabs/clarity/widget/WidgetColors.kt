package com.runtimelabs.clarity.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider

/*
 * Glance renders via RemoteViews and cannot consume MaterialTheme, so the
 * widget can't read core/designsystem/theme/Color.kt directly. Rather than
 * pull in the glance-material3 interop artifact for one small widget, the
 * exact brand hex values are mirrored here by hand — recommended practice
 * for apps with a custom (non-dynamic-color) palette per Glance's own
 * guidance. KEEP IN SYNC WITH Color.kt IF THE PALETTE EVER CHANGES.
 */
internal object WidgetColors {
    val background = ColorProvider(day = Color(0xFFF7F9F9), night = Color(0xFF0D1414))
    val surface = ColorProvider(day = Color(0xFFFEFFFF), night = Color(0xFF132020))
    val primary = ColorProvider(day = Color(0xFF0E7C7B), night = Color(0xFF7FD1CF))
    val onSurfaceVariant = ColorProvider(day = Color(0xFF3F4948), night = Color(0xFFBEC9C8))
    val track = ColorProvider(day = Color(0xFFDCE5E4), night = Color(0xFF1E2E2D))
    val celebration = ColorProvider(day = Color(0xFFE8A87C), night = Color(0xFFF0B98E))
}
