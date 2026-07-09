package com.runtimelabs.clarity.feature.toolkit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool

private data class ReminderContent(
    val titleRes: Int,
    val instructionsRes: Int,
    val icon: ImageVector,
)

private fun contentFor(tool: ToolkitTool): ReminderContent = when (tool) {
    ToolkitTool.PUSH_UPS -> ReminderContent(R.string.tool_push_ups_title, R.string.tool_push_ups_instructions, Icons.Rounded.FitnessCenter)
    ToolkitTool.DRINK_WATER -> ReminderContent(R.string.tool_drink_water_title, R.string.tool_drink_water_instructions, Icons.Rounded.LocalDrink)
    ToolkitTool.CALL_FRIEND -> ReminderContent(R.string.tool_call_friend_title, R.string.tool_call_friend_instructions, Icons.Rounded.Call)
    else -> ReminderContent(R.string.tool_cold_shower_title, R.string.tool_cold_shower_instructions, Icons.Rounded.AcUnit)
}

/**
 * One screen, four tools (cold shower / push-ups / water / call a friend) —
 * the activity itself happens away from the phone, so this is instruction
 * plus a single confirm action, not a live-tracked session.
 */
@Composable
fun ReminderToolScreen(
    tool: ToolkitTool,
    onDone: () -> Unit,
    viewModel: ReminderToolViewModel = hiltViewModel(),
) {
    val content = remember(tool) { contentFor(tool) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = MaterialTheme.spacing.sm),
            ) {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spacing.xl),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                ) {
                    Icon(
                        imageVector = content.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                Text(
                    text = stringResource(content.titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Text(
                    text = stringResource(content.instructionsRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                    .padding(bottom = MaterialTheme.spacing.lg),
            ) {
                ClarityPrimaryButton(
                    text = stringResource(R.string.toolkit_made_it_through),
                    onClick = {
                        viewModel.onMadeItThrough(tool)
                        onDone()
                    },
                )
            }
        }
    }
}
