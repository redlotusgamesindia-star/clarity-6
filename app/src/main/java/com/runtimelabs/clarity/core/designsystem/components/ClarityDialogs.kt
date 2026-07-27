package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shared look for the app's system surfaces — [androidx.compose.material3.AlertDialog]
 * and [androidx.compose.material3.ModalBottomSheet] — applied at each call
 * site through the `shape=`/`containerColor=`/etc. parameters those
 * composables already expose. Deliberately not a wrapper composable: every
 * one of these dialogs already has its confirm/dismiss logic and copy
 * right, so this pass only ever adds styling parameters to the existing
 * calls rather than touching their structure.
 */
object ClarityDialogDefaults {
    /** Centered dialogs (AlertDialog) — full 32dp radius on every corner. */
    val shape: Shape @Composable get() = MaterialTheme.shapes.extraLarge

    /** Sheets docked to the bottom edge — rounded only where they meet open space. */
    val sheetShape: Shape
        @Composable get() = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        )

    val containerColor: Color @Composable get() = MaterialTheme.colorScheme.surface
    val titleContentColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val textContentColor: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val iconContentColor: Color @Composable get() = MaterialTheme.colorScheme.primary
}
