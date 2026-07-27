package com.runtimelabs.clarity.feature.achievement

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.badge.BadgeCategory

/*
 * The domain layer stays locale- and Android-free; this is the one place
 * [Badge] meets display text and iconography — same contract as every
 * other *Labels.kt file in the app (RecoveryLabels.kt for
 * ComebackAchievement, onboarding's plan codes, journey's insight codes).
 *
 * [Badge.descriptionRes] deliberately doubles as both "what you did to earn
 * this" (shown once unlocked) AND "what's still required" (shown while
 * locked) — the criterion is the same sentence either way, so this avoids
 * a second, easy-to-drift string per badge for what would just be the same
 * fact phrased twice.
 */

fun Badge.icon(): ImageVector = when (this) {
    Badge.DAY_1 -> Icons.Rounded.WbSunny
    Badge.DAY_3 -> Icons.Rounded.LocalFireDepartment
    Badge.DAY_7 -> Icons.Rounded.EventAvailable
    Badge.DAY_14 -> Icons.Rounded.Star
    Badge.DAY_21 -> Icons.Rounded.TrendingUp
    Badge.DAY_30 -> Icons.Rounded.EmojiEvents
    Badge.DAY_50 -> Icons.Rounded.WorkspacePremium
    Badge.DAY_100 -> Icons.Rounded.Diamond
    Badge.DAY_365 -> Icons.Rounded.Celebration
    Badge.FIRST_RECOVERY -> Icons.Rounded.Cached
    Badge.FIVE_RECOVERIES -> Icons.Rounded.Shield
    Badge.MORNING_CHECK_IN -> Icons.Rounded.LightMode
    Badge.JOURNAL_WRITER -> Icons.Rounded.EditNote
    Badge.LEARNING_STREAK -> Icons.Rounded.MenuBook
}

@StringRes
fun Badge.titleRes(): Int = when (this) {
    Badge.DAY_1 -> R.string.badge_day_1_title
    Badge.DAY_3 -> R.string.badge_day_3_title
    Badge.DAY_7 -> R.string.badge_day_7_title
    Badge.DAY_14 -> R.string.badge_day_14_title
    Badge.DAY_21 -> R.string.badge_day_21_title
    Badge.DAY_30 -> R.string.badge_day_30_title
    Badge.DAY_50 -> R.string.badge_day_50_title
    Badge.DAY_100 -> R.string.badge_day_100_title
    Badge.DAY_365 -> R.string.badge_day_365_title
    Badge.FIRST_RECOVERY -> R.string.badge_first_recovery_title
    Badge.FIVE_RECOVERIES -> R.string.badge_five_recoveries_title
    Badge.MORNING_CHECK_IN -> R.string.badge_morning_check_in_title
    Badge.JOURNAL_WRITER -> R.string.badge_journal_writer_title
    Badge.LEARNING_STREAK -> R.string.badge_learning_streak_title
}

@StringRes
fun Badge.descriptionRes(): Int = when (this) {
    Badge.DAY_1 -> R.string.badge_day_1_desc
    Badge.DAY_3 -> R.string.badge_day_3_desc
    Badge.DAY_7 -> R.string.badge_day_7_desc
    Badge.DAY_14 -> R.string.badge_day_14_desc
    Badge.DAY_21 -> R.string.badge_day_21_desc
    Badge.DAY_30 -> R.string.badge_day_30_desc
    Badge.DAY_50 -> R.string.badge_day_50_desc
    Badge.DAY_100 -> R.string.badge_day_100_desc
    Badge.DAY_365 -> R.string.badge_day_365_desc
    Badge.FIRST_RECOVERY -> R.string.badge_first_recovery_desc
    Badge.FIVE_RECOVERIES -> R.string.badge_five_recoveries_desc
    Badge.MORNING_CHECK_IN -> R.string.badge_morning_check_in_desc
    Badge.JOURNAL_WRITER -> R.string.badge_journal_writer_desc
    Badge.LEARNING_STREAK -> R.string.badge_learning_streak_desc
}

/**
 * The "motivational quotes unlock" feature: one bespoke line per badge,
 * revealed only once that badge is earned, shown on the unlock celebration
 * and kept visible afterward in the badge's own detail sheet. Distinct from
 * [R.array.daily_quotes] (the always-visible rotating Home quote) — these
 * are earned, not ambient, and collecting them is part of the point of the
 * badge collection.
 */
@StringRes
fun Badge.unlockQuoteRes(): Int = when (this) {
    Badge.DAY_1 -> R.string.badge_day_1_quote
    Badge.DAY_3 -> R.string.badge_day_3_quote
    Badge.DAY_7 -> R.string.badge_day_7_quote
    Badge.DAY_14 -> R.string.badge_day_14_quote
    Badge.DAY_21 -> R.string.badge_day_21_quote
    Badge.DAY_30 -> R.string.badge_day_30_quote
    Badge.DAY_50 -> R.string.badge_day_50_quote
    Badge.DAY_100 -> R.string.badge_day_100_quote
    Badge.DAY_365 -> R.string.badge_day_365_quote
    Badge.FIRST_RECOVERY -> R.string.badge_first_recovery_quote
    Badge.FIVE_RECOVERIES -> R.string.badge_five_recoveries_quote
    Badge.MORNING_CHECK_IN -> R.string.badge_morning_check_in_quote
    Badge.JOURNAL_WRITER -> R.string.badge_journal_writer_quote
    Badge.LEARNING_STREAK -> R.string.badge_learning_streak_quote
}

@StringRes
fun BadgeCategory.titleRes(): Int = when (this) {
    BadgeCategory.STREAK -> R.string.badge_category_streak
    BadgeCategory.RECOVERY -> R.string.badge_category_recovery
    BadgeCategory.DAILY_PRACTICE -> R.string.badge_category_daily_practice
}
