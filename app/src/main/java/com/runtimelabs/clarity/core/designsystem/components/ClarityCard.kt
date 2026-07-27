package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.core.designsystem.theme.extended

/**
 * The app's standard surface (2026 refresh): a layered glass panel instead
 * of a flat Material card. A translucent vertical fill (lighter at the top,
 * as if lit from above) plus a hairline gradient edge do the "floating"
 * work that a drop shadow would have done in the old palette — shadows
 * read as mud on near-black, so this pass drops them for every card and
 * relies on light/translucency cues instead.
 *
 * [glow] adds a soft bloom behind the card for the few moments per screen
 * that should read as important (a hero stat, a milestone, the one primary
 * choice on a step). Leave it off for ordinary rows and lists — see
 * [GlowOrb]'s doc comment for the same restraint rule applied everywhere
 * else.
 */
@Composable
fun ClarityCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    glow: Boolean = false,
    glowColor: Color = MaterialTheme.extended.gradientStart,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val fill = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
        ),
    )
    val edgeBrush = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.02f)),
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Box {
        if (glow) {
            GlowOrb(
                color = glowColor.copy(alpha = 0.22f),
                modifier = Modifier.matchParentSize(),
            )
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Column(
                modifier = modifier
                    .alpha(if (enabled) 1f else 0.5f)
                    .clip(shape)
                    .background(fill)
                    .border(1.dp, edgeBrush, shape)
                    .then(clickModifier)
                    .padding(contentPadding),
                content = content,
            )
        }
    }
}
