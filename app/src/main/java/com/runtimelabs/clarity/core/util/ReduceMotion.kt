package com.runtimelabs.clarity.core.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * True when the user has disabled animations system-wide
 * (animator duration scale = 0). Decorative motion — the breathing indicator,
 * milestone celebrations — must degrade to static/fade when this is set.
 * Read once per composition; toggling it is rare enough that requiring a
 * screen re-entry is acceptable.
 */
@Composable
fun rememberReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
