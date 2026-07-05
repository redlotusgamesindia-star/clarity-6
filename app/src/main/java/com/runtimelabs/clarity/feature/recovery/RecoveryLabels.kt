package com.runtimelabs.clarity.feature.recovery

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.ui.graphics.vector.ImageVector
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.model.RelapseLocation
import com.runtimelabs.clarity.domain.recovery.ComebackAchievement
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItemCode
import com.runtimelabs.clarity.domain.recovery.RecoveryMotivationCode

/*
 * The domain layer stays locale-free; this is the one place its recovery-
 * flow codes meet display text, same contract as every other *Labels.kt
 * file in the app (onboarding's plan codes, journey's insight codes).
 */

@StringRes
fun RelapseLocation.labelRes(): Int = when (this) {
    RelapseLocation.HOME -> R.string.relapse_location_home
    RelapseLocation.WORK_OR_SCHOOL -> R.string.relapse_location_work
    RelapseLocation.TRAVELING -> R.string.relapse_location_traveling
    RelapseLocation.OTHER -> R.string.relapse_location_other
}

@StringRes
fun RecoveryChecklistItemCode.titleRes(): Int = when (this) {
    RecoveryChecklistItemCode.DRINK_WATER -> R.string.checklist_drink_water
    RecoveryChecklistItemCode.TAKE_WALK -> R.string.checklist_take_walk
    RecoveryChecklistItemCode.SHOWER -> R.string.checklist_shower
    RecoveryChecklistItemCode.JOURNAL_IT -> R.string.checklist_journal
    RecoveryChecklistItemCode.BREATHING_EXERCISE -> R.string.checklist_breathing
    RecoveryChecklistItemCode.REACH_OUT -> R.string.checklist_reach_out
    RecoveryChecklistItemCode.PLAN_NEXT_HOUR -> R.string.checklist_plan_next_hour
    RecoveryChecklistItemCode.WIND_DOWN -> R.string.checklist_wind_down
}

@StringRes
fun RecoveryMotivationCode.textRes(): Int = when (this) {
    RecoveryMotivationCode.DAY_1 -> R.string.motivation_day_1
    RecoveryMotivationCode.DAY_2 -> R.string.motivation_day_2
    RecoveryMotivationCode.DAY_3 -> R.string.motivation_day_3
    RecoveryMotivationCode.DAY_4 -> R.string.motivation_day_4
    RecoveryMotivationCode.DAY_5 -> R.string.motivation_day_5
    RecoveryMotivationCode.DAY_6 -> R.string.motivation_day_6
    RecoveryMotivationCode.DAY_7 -> R.string.motivation_day_7
    RecoveryMotivationCode.WEEK_2 -> R.string.motivation_week_2
    RecoveryMotivationCode.WEEK_3 -> R.string.motivation_week_3
    RecoveryMotivationCode.MONTH_1 -> R.string.motivation_month_1
    RecoveryMotivationCode.ONGOING -> R.string.motivation_ongoing
}

fun ComebackAchievement.icon(): ImageVector = when (this) {
    ComebackAchievement.STARTED_AGAIN -> Icons.Rounded.Spa
    ComebackAchievement.DIDNT_QUIT -> Icons.Rounded.Favorite
    ComebackAchievement.FIRST_WEEK_BACK -> Icons.Rounded.LocalFireDepartment
    ComebackAchievement.STRONGER_THAN_BEFORE -> Icons.Rounded.Bolt
    ComebackAchievement.BEAT_PREVIOUS_RECORD -> Icons.Rounded.EmojiEvents
}

@StringRes
fun ComebackAchievement.titleRes(): Int = when (this) {
    ComebackAchievement.STARTED_AGAIN -> R.string.achievement_started_again
    ComebackAchievement.DIDNT_QUIT -> R.string.achievement_didnt_quit
    ComebackAchievement.FIRST_WEEK_BACK -> R.string.achievement_first_week_back
    ComebackAchievement.STRONGER_THAN_BEFORE -> R.string.achievement_stronger_than_before
    ComebackAchievement.BEAT_PREVIOUS_RECORD -> R.string.achievement_beat_record
}

@StringRes
fun ComebackAchievement.descriptionRes(): Int = when (this) {
    ComebackAchievement.STARTED_AGAIN -> R.string.achievement_started_again_desc
    ComebackAchievement.DIDNT_QUIT -> R.string.achievement_didnt_quit_desc
    ComebackAchievement.FIRST_WEEK_BACK -> R.string.achievement_first_week_back_desc
    ComebackAchievement.STRONGER_THAN_BEFORE -> R.string.achievement_stronger_than_before_desc
    ComebackAchievement.BEAT_PREVIOUS_RECORD -> R.string.achievement_beat_record_desc
}
