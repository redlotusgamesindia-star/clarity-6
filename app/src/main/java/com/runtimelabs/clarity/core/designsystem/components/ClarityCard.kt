package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The app's standard surface. Low tonal elevation + hairline outline instead
 * of drop shadows: reads as "premium stationery", stays quiet in dark mode
 * (where shadows are invisible and tonal overlays can look muddy).
 */
@Composable
fun ClarityCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val color = MaterialTheme.colorScheme.surface
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))

    if (onClick != null) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            color = color,
            tonalElevation = 1.dp,
            border = border,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(
            shape = shape,
            color = color,
            tonalElevation = 1.dp,
            border = border,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}
