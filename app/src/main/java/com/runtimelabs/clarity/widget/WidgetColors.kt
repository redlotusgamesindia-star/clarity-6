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
 *
 * Synced for the 2026 "neon minimal" refresh: night now mirrors
 * ClarityDarkColorScheme's void-black/neon-cyan pair instead of the old
 * teal palette; day mirrors ClarityLightColorScheme unchanged in spirit.
 * Glance widgets can't do gradients, blur, or animation, so this stays a
 * flat-color mirror only — the glass/glow treatment is deliberately a
 * full-app-only signature, not attempted here.
 */
internal object WidgetColors {
    val background = ColorProvider(day = Color(0xFFF3F6FB), night = Color(0xFF08090B))
    val surface = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF111318))
    val primary = ColorProvider(day = Color(0xFF0E7FB0), night = Color(0xFF4FC8FF))
    val onSurfaceVariant = ColorProvider(day = Color(0xFF565F70), night = Color(0xFFA7B0C0))
    val track = ColorProvider(day = Color(0xFFE7ECF5), night = Color(0xFF1B1F27))
    val celebration = ColorProvider(day = Color(0xFFD9791F), night = Color(0xFFFFC38A))
}
