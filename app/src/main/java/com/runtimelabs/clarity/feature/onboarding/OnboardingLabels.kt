package com.runtimelabs.clarity.feature.onboarding

import androidx.annotation.StringRes
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.model.AgeRange
import com.runtimelabs.clarity.domain.model.GenderIdentity
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.PlanCategory
import com.runtimelabs.clarity.domain.model.PlanItemCode
import com.runtimelabs.clarity.domain.model.PreviousStreak
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.model.SleepSchedule
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.model.UsageFrequency
import com.runtimelabs.clarity.domain.model.YearsAddicted

/*
 * The domain layer is locale-free; this file is the single place where
 * domain values meet display text. Exhaustive `when`s mean adding an enum
 * value without copy is a compile error, not a runtime blank.
 */

@StringRes
fun OnboardingQuestion.titleRes(): Int = when (this) {
    OnboardingQuestion.AGE -> R.string.q_age_title
    OnboardingQuestion.GENDER -> R.string.q_gender_title
    OnboardingQuestion.YEARS_ADDICTED -> R.string.q_years_title
    OnboardingQuestion.FREQUENCY -> R.string.q_frequency_title
    OnboardingQuestion.MAIN_TRIGGER -> R.string.q_trigger_title
    OnboardingQuestion.URGE_TIME -> R.string.q_urge_time_title
    OnboardingQuestion.SLEEP_SCHEDULE -> R.string.q_sleep_title
    OnboardingQuestion.PREVIOUS_STREAK -> R.string.q_prev_streak_title
    OnboardingQuestion.GOAL -> R.string.q_goal_title
    OnboardingQuestion.REASONS -> R.string.q_reasons_title
    OnboardingQuestion.MOTIVATION -> R.string.q_motivation_title
}

@StringRes
fun OnboardingQuestion.subtitleResOrNull(): Int? = when (this) {
    OnboardingQuestion.AGE -> R.string.q_age_subtitle
    OnboardingQuestion.GENDER -> R.string.q_gender_subtitle
    OnboardingQuestion.YEARS_ADDICTED -> R.string.q_years_subtitle
    OnboardingQuestion.FREQUENCY -> R.string.q_frequency_subtitle
    OnboardingQuestion.MAIN_TRIGGER -> R.string.q_trigger_subtitle
    OnboardingQuestion.URGE_TIME -> null
    OnboardingQuestion.SLEEP_SCHEDULE -> R.string.q_sleep_subtitle
    OnboardingQuestion.PREVIOUS_STREAK -> R.string.q_prev_streak_subtitle
    OnboardingQuestion.GOAL -> null
    OnboardingQuestion.REASONS -> R.string.q_reasons_subtitle
    OnboardingQuestion.MOTIVATION -> R.string.q_motivation_subtitle
}

@StringRes
fun AgeRange.labelRes(): Int = when (this) {
    AgeRange.AGE_18_24 -> R.string.age_18_24
    AgeRange.AGE_25_34 -> R.string.age_25_34
    AgeRange.AGE_35_44 -> R.string.age_35_44
    AgeRange.AGE_45_54 -> R.string.age_45_54
    AgeRange.AGE_55_PLUS -> R.string.age_55_plus
}

@StringRes
fun GenderIdentity.labelRes(): Int = when (this) {
    GenderIdentity.MALE -> R.string.gender_male
    GenderIdentity.FEMALE -> R.string.gender_female
    GenderIdentity.NON_BINARY -> R.string.gender_non_binary
    GenderIdentity.PREFER_NOT_TO_SAY -> R.string.gender_prefer_not
}

@StringRes
fun YearsAddicted.labelRes(): Int = when (this) {
    YearsAddicted.LESS_THAN_ONE -> R.string.years_lt_1
    YearsAddicted.ONE_TO_THREE -> R.string.years_1_3
    YearsAddicted.THREE_TO_FIVE -> R.string.years_3_5
    YearsAddicted.FIVE_TO_TEN -> R.string.years_5_10
    YearsAddicted.OVER_TEN -> R.string.years_gt_10
}

@StringRes
fun UsageFrequency.labelRes(): Int = when (this) {
    UsageFrequency.MULTIPLE_DAILY -> R.string.freq_multiple_daily
    UsageFrequency.DAILY -> R.string.freq_daily
    UsageFrequency.SEVERAL_PER_WEEK -> R.string.freq_several_week
    UsageFrequency.WEEKLY -> R.string.freq_weekly
    UsageFrequency.FEW_PER_MONTH -> R.string.freq_few_month
}

@StringRes
fun MainTrigger.labelRes(): Int = when (this) {
    MainTrigger.STRESS -> R.string.trigger_stress
    MainTrigger.BOREDOM -> R.string.trigger_boredom
    MainTrigger.LONELINESS -> R.string.trigger_loneliness
    MainTrigger.FATIGUE -> R.string.trigger_fatigue
    MainTrigger.SOCIAL_MEDIA -> R.string.trigger_social_media
    MainTrigger.DIFFICULT_EMOTIONS -> R.string.trigger_emotions
}

@StringRes
fun UrgeTime.labelRes(): Int = when (this) {
    UrgeTime.MORNING -> R.string.urge_morning
    UrgeTime.AFTERNOON -> R.string.urge_afternoon
    UrgeTime.EVENING -> R.string.urge_evening
    UrgeTime.LATE_NIGHT -> R.string.urge_late_night
    UrgeTime.UNPREDICTABLE -> R.string.urge_unpredictable
}

@StringRes
fun SleepSchedule.labelRes(): Int = when (this) {
    SleepSchedule.EARLY_CONSISTENT -> R.string.sleep_early
    SleepSchedule.REGULAR -> R.string.sleep_regular
    SleepSchedule.NIGHT_OWL -> R.string.sleep_night_owl
    SleepSchedule.IRREGULAR -> R.string.sleep_irregular
}

@StringRes
fun PreviousStreak.labelRes(): Int = when (this) {
    PreviousStreak.NEVER_TRIED -> R.string.streak_never
    PreviousStreak.DAYS_1_7 -> R.string.streak_1_7
    PreviousStreak.WEEKS_1_4 -> R.string.streak_8_30
    PreviousStreak.MONTHS_1_3 -> R.string.streak_31_90
    PreviousStreak.OVER_90_DAYS -> R.string.streak_90_plus
}

@StringRes
fun RecoveryGoal.labelRes(): Int = when (this) {
    RecoveryGoal.QUIT_COMPLETELY -> R.string.goal_quit
    RecoveryGoal.REBOOT_90_DAYS -> R.string.goal_reboot
    RecoveryGoal.REDUCE_GRADUALLY -> R.string.goal_reduce
}

@StringRes
fun RecoveryGoal.supportingRes(): Int = when (this) {
    RecoveryGoal.QUIT_COMPLETELY -> R.string.goal_quit_support
    RecoveryGoal.REBOOT_90_DAYS -> R.string.goal_reboot_support
    RecoveryGoal.REDUCE_GRADUALLY -> R.string.goal_reduce_support
}

@StringRes
fun ReasonToQuit.labelRes(): Int = when (this) {
    ReasonToQuit.MENTAL_CLARITY -> R.string.reason_mental_clarity
    ReasonToQuit.RELATIONSHIPS -> R.string.reason_relationships
    ReasonToQuit.SELF_RESPECT -> R.string.reason_self_respect
    ReasonToQuit.ENERGY_PRODUCTIVITY -> R.string.reason_energy
    ReasonToQuit.FAITH_VALUES -> R.string.reason_faith
    ReasonToQuit.SEXUAL_HEALTH -> R.string.reason_sexual_health
    ReasonToQuit.RECLAIM_TIME -> R.string.reason_time
}

@StringRes
fun PlanCategory.labelRes(): Int = when (this) {
    PlanCategory.FOUNDATION -> R.string.cat_foundation
    PlanCategory.TRIGGER_DEFENSE -> R.string.cat_trigger_defense
    PlanCategory.ENVIRONMENT -> R.string.cat_environment
    PlanCategory.SLEEP -> R.string.cat_sleep
    PlanCategory.MINDSET -> R.string.cat_mindset
}

@StringRes
fun PlanItemCode.titleRes(): Int = when (this) {
    PlanItemCode.DAILY_CHECKIN -> R.string.plan_daily_checkin_title
    PlanItemCode.LEARN_URGE_TOOLKIT -> R.string.plan_learn_urge_toolkit_title
    PlanItemCode.DEFINE_YOUR_WHY -> R.string.plan_define_your_why_title
    PlanItemCode.FIRST_MILESTONE_FOCUS -> R.string.plan_first_milestone_focus_title
    PlanItemCode.ABSTINENCE_IDENTITY -> R.string.plan_abstinence_identity_title
    PlanItemCode.REBOOT_90_FRAME -> R.string.plan_reboot_90_frame_title
    PlanItemCode.REDUCTION_LADDER -> R.string.plan_reduction_ladder_title
    PlanItemCode.HIGH_RISK_MOMENT_MAP -> R.string.plan_high_risk_moment_map_title
    PlanItemCode.DEVICE_FRICTION_SETUP -> R.string.plan_device_friction_setup_title
    PlanItemCode.STRESS_RESET_BREATHING -> R.string.plan_stress_reset_breathing_title
    PlanItemCode.BOREDOM_REPLACEMENT_LIST -> R.string.plan_boredom_replacement_list_title
    PlanItemCode.CONNECTION_PLAN -> R.string.plan_connection_plan_title
    PlanItemCode.FATIGUE_ENERGY_AUDIT -> R.string.plan_fatigue_energy_audit_title
    PlanItemCode.SOCIAL_MEDIA_HYGIENE -> R.string.plan_social_media_hygiene_title
    PlanItemCode.EMOTION_JOURNALING -> R.string.plan_emotion_journaling_title
    PlanItemCode.IF_THEN_PLANS -> R.string.plan_if_then_plans_title
    PlanItemCode.LATE_NIGHT_DEVICE_CURFEW -> R.string.plan_late_night_device_curfew_title
    PlanItemCode.MORNING_LAUNCH_ROUTINE -> R.string.plan_morning_launch_routine_title
    PlanItemCode.EVENING_TRANSITION_RITUAL -> R.string.plan_evening_transition_ritual_title
    PlanItemCode.AFTERNOON_RESET_WALK -> R.string.plan_afternoon_reset_walk_title
    PlanItemCode.WIND_DOWN_ROUTINE -> R.string.plan_wind_down_routine_title
    PlanItemCode.SLEEP_ANCHOR_TIME -> R.string.plan_sleep_anchor_time_title
    PlanItemCode.START_TINY_COMMITMENT -> R.string.plan_start_tiny_commitment_title
    PlanItemCode.CHANNEL_MOMENTUM -> R.string.plan_channel_momentum_title
    PlanItemCode.STREAK_PATTERN_REVIEW -> R.string.plan_streak_pattern_review_title
}

@StringRes
fun PlanItemCode.descriptionRes(): Int = when (this) {
    PlanItemCode.DAILY_CHECKIN -> R.string.plan_daily_checkin_desc
    PlanItemCode.LEARN_URGE_TOOLKIT -> R.string.plan_learn_urge_toolkit_desc
    PlanItemCode.DEFINE_YOUR_WHY -> R.string.plan_define_your_why_desc
    PlanItemCode.FIRST_MILESTONE_FOCUS -> R.string.plan_first_milestone_focus_desc
    PlanItemCode.ABSTINENCE_IDENTITY -> R.string.plan_abstinence_identity_desc
    PlanItemCode.REBOOT_90_FRAME -> R.string.plan_reboot_90_frame_desc
    PlanItemCode.REDUCTION_LADDER -> R.string.plan_reduction_ladder_desc
    PlanItemCode.HIGH_RISK_MOMENT_MAP -> R.string.plan_high_risk_moment_map_desc
    PlanItemCode.DEVICE_FRICTION_SETUP -> R.string.plan_device_friction_setup_desc
    PlanItemCode.STRESS_RESET_BREATHING -> R.string.plan_stress_reset_breathing_desc
    PlanItemCode.BOREDOM_REPLACEMENT_LIST -> R.string.plan_boredom_replacement_list_desc
    PlanItemCode.CONNECTION_PLAN -> R.string.plan_connection_plan_desc
    PlanItemCode.FATIGUE_ENERGY_AUDIT -> R.string.plan_fatigue_energy_audit_desc
    PlanItemCode.SOCIAL_MEDIA_HYGIENE -> R.string.plan_social_media_hygiene_desc
    PlanItemCode.EMOTION_JOURNALING -> R.string.plan_emotion_journaling_desc
    PlanItemCode.IF_THEN_PLANS -> R.string.plan_if_then_plans_desc
    PlanItemCode.LATE_NIGHT_DEVICE_CURFEW -> R.string.plan_late_night_device_curfew_desc
    PlanItemCode.MORNING_LAUNCH_ROUTINE -> R.string.plan_morning_launch_routine_desc
    PlanItemCode.EVENING_TRANSITION_RITUAL -> R.string.plan_evening_transition_ritual_desc
    PlanItemCode.AFTERNOON_RESET_WALK -> R.string.plan_afternoon_reset_walk_desc
    PlanItemCode.WIND_DOWN_ROUTINE -> R.string.plan_wind_down_routine_desc
    PlanItemCode.SLEEP_ANCHOR_TIME -> R.string.plan_sleep_anchor_time_desc
    PlanItemCode.START_TINY_COMMITMENT -> R.string.plan_start_tiny_commitment_desc
    PlanItemCode.CHANNEL_MOMENTUM -> R.string.plan_channel_momentum_desc
    PlanItemCode.STREAK_PATTERN_REVIEW -> R.string.plan_streak_pattern_review_desc
}
