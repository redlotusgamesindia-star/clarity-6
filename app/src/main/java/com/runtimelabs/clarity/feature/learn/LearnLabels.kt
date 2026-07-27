package com.runtimelabs.clarity.feature.learn

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.ui.graphics.vector.ImageVector
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.learn.LearnArticle
import com.runtimelabs.clarity.domain.learn.LearnCategory

/*
 * The domain layer stays locale-free; this is the one place [LearnArticle]
 * and [LearnCategory] meet display text and iconography — same *Labels.kt
 * contract as every other closed vocabulary in this app.
 */

fun LearnCategory.icon(): ImageVector = when (this) {
    LearnCategory.UNDERSTANDING_URGES -> Icons.Rounded.Psychology
    LearnCategory.BUILDING_NEW_HABITS -> Icons.Rounded.Cached
    LearnCategory.STAYING_THE_COURSE -> Icons.Rounded.Favorite
}

@StringRes
fun LearnCategory.titleRes(): Int = when (this) {
    LearnCategory.UNDERSTANDING_URGES -> R.string.learn_category_understanding_urges
    LearnCategory.BUILDING_NEW_HABITS -> R.string.learn_category_building_habits
    LearnCategory.STAYING_THE_COURSE -> R.string.learn_category_staying_course
}

fun LearnArticle.icon(): ImageVector = when (this) {
    LearnArticle.WHAT_IS_AN_URGE -> Icons.Rounded.Bolt
    LearnArticle.THE_URGE_SURFING_TECHNIQUE -> Icons.Rounded.SelfImprovement
    LearnArticle.WHY_WILLPOWER_ALONE_FALLS_SHORT -> Icons.Rounded.Psychology
    LearnArticle.HALT_YOUR_MOST_COMMON_TRIGGERS -> Icons.Rounded.Flag
    LearnArticle.HOW_HABITS_ACTUALLY_CHANGE -> Icons.Rounded.Cached
    LearnArticle.REPLACING_NOT_JUST_REMOVING -> Icons.Rounded.AutoAwesome
    LearnArticle.DESIGNING_YOUR_ENVIRONMENT -> Icons.Rounded.Home
    LearnArticle.SLEEP_AND_EXERCISE_AS_INFRASTRUCTURE -> Icons.Rounded.Bedtime
    LearnArticle.WHAT_A_RELAPSE_ACTUALLY_MEANS -> Icons.Rounded.Favorite
    LearnArticle.SELF_COMPASSION_ISNT_SELF_INDULGENCE -> Icons.Rounded.Spa
    LearnArticle.WHY_ISOLATION_MAKES_URGES_LOUDER -> Icons.Rounded.People
    LearnArticle.PLAYING_THE_TAPE_FORWARD -> Icons.Rounded.Insights
}

@StringRes
fun LearnArticle.titleRes(): Int = when (this) {
    LearnArticle.WHAT_IS_AN_URGE -> R.string.learn_urge_what_is_title
    LearnArticle.THE_URGE_SURFING_TECHNIQUE -> R.string.learn_urge_surfing_title
    LearnArticle.WHY_WILLPOWER_ALONE_FALLS_SHORT -> R.string.learn_willpower_title
    LearnArticle.HALT_YOUR_MOST_COMMON_TRIGGERS -> R.string.learn_halt_title
    LearnArticle.HOW_HABITS_ACTUALLY_CHANGE -> R.string.learn_habits_change_title
    LearnArticle.REPLACING_NOT_JUST_REMOVING -> R.string.learn_replacing_title
    LearnArticle.DESIGNING_YOUR_ENVIRONMENT -> R.string.learn_environment_title
    LearnArticle.SLEEP_AND_EXERCISE_AS_INFRASTRUCTURE -> R.string.learn_sleep_exercise_title
    LearnArticle.WHAT_A_RELAPSE_ACTUALLY_MEANS -> R.string.learn_relapse_meaning_title
    LearnArticle.SELF_COMPASSION_ISNT_SELF_INDULGENCE -> R.string.learn_self_compassion_title
    LearnArticle.WHY_ISOLATION_MAKES_URGES_LOUDER -> R.string.learn_isolation_title
    LearnArticle.PLAYING_THE_TAPE_FORWARD -> R.string.learn_tape_forward_title
}

/** One line, shown in the list — the hook, not a summary of the whole piece. */
@StringRes
fun LearnArticle.summaryRes(): Int = when (this) {
    LearnArticle.WHAT_IS_AN_URGE -> R.string.learn_urge_what_is_summary
    LearnArticle.THE_URGE_SURFING_TECHNIQUE -> R.string.learn_urge_surfing_summary
    LearnArticle.WHY_WILLPOWER_ALONE_FALLS_SHORT -> R.string.learn_willpower_summary
    LearnArticle.HALT_YOUR_MOST_COMMON_TRIGGERS -> R.string.learn_halt_summary
    LearnArticle.HOW_HABITS_ACTUALLY_CHANGE -> R.string.learn_habits_change_summary
    LearnArticle.REPLACING_NOT_JUST_REMOVING -> R.string.learn_replacing_summary
    LearnArticle.DESIGNING_YOUR_ENVIRONMENT -> R.string.learn_environment_summary
    LearnArticle.SLEEP_AND_EXERCISE_AS_INFRASTRUCTURE -> R.string.learn_sleep_exercise_summary
    LearnArticle.WHAT_A_RELAPSE_ACTUALLY_MEANS -> R.string.learn_relapse_meaning_summary
    LearnArticle.SELF_COMPASSION_ISNT_SELF_INDULGENCE -> R.string.learn_self_compassion_summary
    LearnArticle.WHY_ISOLATION_MAKES_URGES_LOUDER -> R.string.learn_isolation_summary
    LearnArticle.PLAYING_THE_TAPE_FORWARD -> R.string.learn_tape_forward_summary
}

/** The full reader body — a few short paragraphs, `\n\n`-separated. */
@StringRes
fun LearnArticle.bodyRes(): Int = when (this) {
    LearnArticle.WHAT_IS_AN_URGE -> R.string.learn_urge_what_is_body
    LearnArticle.THE_URGE_SURFING_TECHNIQUE -> R.string.learn_urge_surfing_body
    LearnArticle.WHY_WILLPOWER_ALONE_FALLS_SHORT -> R.string.learn_willpower_body
    LearnArticle.HALT_YOUR_MOST_COMMON_TRIGGERS -> R.string.learn_halt_body
    LearnArticle.HOW_HABITS_ACTUALLY_CHANGE -> R.string.learn_habits_change_body
    LearnArticle.REPLACING_NOT_JUST_REMOVING -> R.string.learn_replacing_body
    LearnArticle.DESIGNING_YOUR_ENVIRONMENT -> R.string.learn_environment_body
    LearnArticle.SLEEP_AND_EXERCISE_AS_INFRASTRUCTURE -> R.string.learn_sleep_exercise_body
    LearnArticle.WHAT_A_RELAPSE_ACTUALLY_MEANS -> R.string.learn_relapse_meaning_body
    LearnArticle.SELF_COMPASSION_ISNT_SELF_INDULGENCE -> R.string.learn_self_compassion_body
    LearnArticle.WHY_ISOLATION_MAKES_URGES_LOUDER -> R.string.learn_isolation_body
    LearnArticle.PLAYING_THE_TAPE_FORWARD -> R.string.learn_tape_forward_body
}

/** Roughly how long the article takes to read, for the list row — a fixed estimate, not measured live. */
fun LearnArticle.readMinutes(): Int = 2
