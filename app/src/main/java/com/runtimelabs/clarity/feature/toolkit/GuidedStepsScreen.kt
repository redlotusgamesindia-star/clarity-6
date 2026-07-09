package com.runtimelabs.clarity.feature.toolkit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool

const val EXERCISE_GROUNDING = "grounding_54321"
const val EXERCISE_MUSCLE = "muscle_release"
const val EXERCISE_DISTRACTION = "distraction_ideas"

private data class GuidedExercise(
    val titleRes: Int,
    val stepsArrayRes: Int,
    val icon: ImageVector,
    val tool: ToolkitTool,
)

private fun exerciseFor(code: String): GuidedExercise = when (code) {
    EXERCISE_MUSCLE -> GuidedExercise(
        titleRes = R.string.toolkit_muscle_title,
        stepsArrayRes = R.array.muscle_steps,
        icon = Icons.Rounded.SelfImprovement,
        tool = ToolkitTool.MUSCLE_RELAXATION,
    )
    EXERCISE_DISTRACTION -> GuidedExercise(
        titleRes = R.string.tool_distraction_ideas_title,
        stepsArrayRes = R.array.distraction_ideas,
        icon = Icons.Rounded.Lightbulb,
        tool = ToolkitTool.DISTRACTION_IDEAS,
    )
    else -> GuidedExercise(
        titleRes = R.string.toolkit_grounding_title,
        stepsArrayRes = R.array.grounding_steps,
        icon = Icons.Rounded.Visibility,
        tool = ToolkitTool.GROUNDING,
    )
}

/**
 * One instruction at a time, advanced by the user — no per-step timers.
 * Someone anxious shouldn't be raced by a countdown, and self-pacing is
 * what keeps this usable under TalkBack and reduce-motion alike. Now also
 * backs Distraction Ideas, sharing the exact same self-paced mechanism —
 * a list of things to try is really the same shape as a list of steps.
 */
@Composable
fun GuidedStepsScreen(
    exerciseCode: String,
    onDone: () -> Unit,
    viewModel: GuidedStepsViewModel = hiltViewModel(),
) {
    val exercise = remember(exerciseCode) { exerciseFor(exerciseCode) }
    val steps = stringArrayResource(exercise.stepsArrayRes)
    var index by rememberSaveable { mutableIntStateOf(0) }
    val reduceMotion = rememberReduceMotionEnabled()

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
                Text(
                    text = stringResource(exercise.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.guided_step_progress, index + 1, steps.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(MaterialTheme.spacing.sm))
            }

            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    if (reduceMotion) {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                    } else {
                        (slideInVertically(tween(300)) { it / 8 } + fadeIn(tween(300))) togetherWith
                            fadeOut(tween(180))
                    }
                },
                label = "guidedStep",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { stepIndex ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.spacing.xl),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                CircleShape,
                            ),
                    ) {
                        Icon(
                            imageVector = exercise.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.xl))
                    Text(
                        text = steps.getOrElse(stepIndex) { "" },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                    .padding(bottom = MaterialTheme.spacing.lg),
            ) {
                ClarityPrimaryButton(
                    text = stringResource(
                        if (index == steps.lastIndex) R.string.toolkit_made_it_through else R.string.guided_next,
                    ),
                    onClick = {
                        if (index < steps.lastIndex) {
                            index++
                        } else {
                            viewModel.onFinished(exercise.tool)
                            onDone()
                        }
                    },
                )
            }
        }
    }
}
