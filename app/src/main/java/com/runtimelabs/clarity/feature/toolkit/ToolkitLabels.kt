package com.runtimelabs.clarity.feature.toolkit

import androidx.annotation.StringRes
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool

/**
 * The domain layer stays locale-free; this is the one place [ToolkitTool]
 * meets display text, same contract as every other *Labels.kt file in the
 * app.
 */
@StringRes
fun ToolkitTool.titleRes(): Int = when (this) {
    ToolkitTool.BREATHING_OPEN -> R.string.toolkit_breathe_title
    ToolkitTool.BREATHING_30S -> R.string.tool_breathing_30s_title
    ToolkitTool.BREATHING_60S -> R.string.tool_breathing_60s_title
    ToolkitTool.BREATHING_2MIN -> R.string.tool_breathing_2min_title
    ToolkitTool.COLD_SHOWER -> R.string.tool_cold_shower_title
    ToolkitTool.WALK_OUTSIDE -> R.string.tool_walk_outside_title
    ToolkitTool.PUSH_UPS -> R.string.tool_push_ups_title
    ToolkitTool.DRINK_WATER -> R.string.tool_drink_water_title
    ToolkitTool.CALL_FRIEND -> R.string.tool_call_friend_title
    ToolkitTool.WRITE_JOURNAL -> R.string.tool_write_journal_title
    ToolkitTool.GROUNDING -> R.string.toolkit_grounding_title
    ToolkitTool.MUSCLE_RELAXATION -> R.string.toolkit_muscle_title
    ToolkitTool.QUICK_REFRAME -> R.string.toolkit_reframe_title
    ToolkitTool.MOTIVATION_WALL -> R.string.tool_motivation_wall_title
    ToolkitTool.DISTRACTION_IDEAS -> R.string.tool_distraction_ideas_title
}
