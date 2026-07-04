package com.runtimelabs.clarity.domain.plan

import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.PlanCategory
import com.runtimelabs.clarity.domain.model.PlanItemCode
import com.runtimelabs.clarity.domain.model.PreviousStreak
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.model.RecoveryPlan
import com.runtimelabs.clarity.domain.model.RecoveryPlanItem
import com.runtimelabs.clarity.domain.model.RecoveryProfile
import com.runtimelabs.clarity.domain.model.SleepSchedule
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.model.UsageFrequency
import javax.inject.Inject

/**
 * Builds a personalized recovery plan from an onboarding profile.
 *
 * Design contract (mirrors AD-4, the explainable-score decision):
 *  - **Deterministic**: same profile -> identical plan, always. No randomness,
 *    no time dependence. Verified by unit tests.
 *  - **Explainable**: every item traces to a specific answer. A future
 *    "why is this in my plan?" UI can show the mapping.
 *  - **Pure Kotlin**: no Android imports; runs in plain JVM unit tests.
 *
 * Rule groups run in a fixed order; [add] silently ignores duplicate codes so
 * overlapping rules (e.g. fatigue trigger + night-owl sleep both wanting a
 * wind-down) compose safely.
 */
class RecoveryPlanGenerator @Inject constructor() {

    fun generate(profile: RecoveryProfile): RecoveryPlan {
        val collected = LinkedHashMap<PlanItemCode, PlanCategory>()

        fun add(code: PlanItemCode, category: PlanCategory) {
            collected.putIfAbsent(code, category)
        }

        // 1) Foundation — everyone starts with the same three anchors plus a
        //    personalized first milestone.
        add(PlanItemCode.DAILY_CHECKIN, PlanCategory.FOUNDATION)
        add(PlanItemCode.LEARN_URGE_TOOLKIT, PlanCategory.FOUNDATION)
        add(PlanItemCode.DEFINE_YOUR_WHY, PlanCategory.FOUNDATION)
        add(PlanItemCode.FIRST_MILESTONE_FOCUS, PlanCategory.FOUNDATION)

        // 2) Goal framing.
        when (profile.goal) {
            RecoveryGoal.QUIT_COMPLETELY -> add(PlanItemCode.ABSTINENCE_IDENTITY, PlanCategory.MINDSET)
            RecoveryGoal.REBOOT_90_DAYS -> add(PlanItemCode.REBOOT_90_FRAME, PlanCategory.MINDSET)
            RecoveryGoal.REDUCE_GRADUALLY -> add(PlanItemCode.REDUCTION_LADDER, PlanCategory.MINDSET)
        }

        // 3) Frequency severity — daily-or-more habits get risk mapping plus
        //    friction; several-per-week gets friction alone.
        when (profile.frequency) {
            UsageFrequency.MULTIPLE_DAILY, UsageFrequency.DAILY -> {
                add(PlanItemCode.HIGH_RISK_MOMENT_MAP, PlanCategory.TRIGGER_DEFENSE)
                add(PlanItemCode.DEVICE_FRICTION_SETUP, PlanCategory.ENVIRONMENT)
            }
            UsageFrequency.SEVERAL_PER_WEEK ->
                add(PlanItemCode.DEVICE_FRICTION_SETUP, PlanCategory.ENVIRONMENT)
            UsageFrequency.WEEKLY, UsageFrequency.FEW_PER_MONTH -> Unit
        }

        // 4) Primary trigger defense.
        when (profile.mainTrigger) {
            MainTrigger.STRESS -> add(PlanItemCode.STRESS_RESET_BREATHING, PlanCategory.TRIGGER_DEFENSE)
            MainTrigger.BOREDOM -> add(PlanItemCode.BOREDOM_REPLACEMENT_LIST, PlanCategory.TRIGGER_DEFENSE)
            MainTrigger.LONELINESS -> add(PlanItemCode.CONNECTION_PLAN, PlanCategory.TRIGGER_DEFENSE)
            MainTrigger.FATIGUE -> {
                add(PlanItemCode.FATIGUE_ENERGY_AUDIT, PlanCategory.TRIGGER_DEFENSE)
                add(PlanItemCode.WIND_DOWN_ROUTINE, PlanCategory.SLEEP)
            }
            MainTrigger.SOCIAL_MEDIA -> add(PlanItemCode.SOCIAL_MEDIA_HYGIENE, PlanCategory.TRIGGER_DEFENSE)
            MainTrigger.DIFFICULT_EMOTIONS -> add(PlanItemCode.EMOTION_JOURNALING, PlanCategory.TRIGGER_DEFENSE)
        }

        // 5) Urge-time routine.
        when (profile.strongestUrgeTime) {
            UrgeTime.LATE_NIGHT -> {
                add(PlanItemCode.LATE_NIGHT_DEVICE_CURFEW, PlanCategory.ENVIRONMENT)
                add(PlanItemCode.WIND_DOWN_ROUTINE, PlanCategory.SLEEP)
            }
            UrgeTime.MORNING -> add(PlanItemCode.MORNING_LAUNCH_ROUTINE, PlanCategory.ENVIRONMENT)
            UrgeTime.EVENING -> add(PlanItemCode.EVENING_TRANSITION_RITUAL, PlanCategory.ENVIRONMENT)
            UrgeTime.AFTERNOON -> add(PlanItemCode.AFTERNOON_RESET_WALK, PlanCategory.ENVIRONMENT)
            UrgeTime.UNPREDICTABLE -> add(PlanItemCode.IF_THEN_PLANS, PlanCategory.TRIGGER_DEFENSE)
        }

        // 6) Sleep structure for late or chaotic schedules.
        when (profile.sleepSchedule) {
            SleepSchedule.NIGHT_OWL, SleepSchedule.IRREGULAR ->
                add(PlanItemCode.SLEEP_ANCHOR_TIME, PlanCategory.SLEEP)
            SleepSchedule.EARLY_CONSISTENT, SleepSchedule.REGULAR -> Unit
        }

        // 7) History-informed mindset work.
        if (profile.previousStreak == PreviousStreak.MONTHS_1_3 ||
            profile.previousStreak == PreviousStreak.OVER_90_DAYS
        ) {
            add(PlanItemCode.STREAK_PATTERN_REVIEW, PlanCategory.MINDSET)
        }

        // 8) Motivation calibration — meet the user where they are.
        when {
            profile.motivationLevel <= LOW_MOTIVATION_MAX ->
                add(PlanItemCode.START_TINY_COMMITMENT, PlanCategory.MINDSET)
            profile.motivationLevel >= HIGH_MOTIVATION_MIN ->
                add(PlanItemCode.CHANNEL_MOMENTUM, PlanCategory.MINDSET)
        }

        // Order: category display rank, then rule insertion order within it.
        val items = collected.entries
            .sortedBy { it.value.displayRank }
            .mapIndexed { index, entry ->
                RecoveryPlanItem(code = entry.key, category = entry.value, orderIndex = index)
            }

        return RecoveryPlan(
            firstMilestoneDays = firstMilestone(profile.previousStreak),
            focusAreas = focusAreas(items),
            items = items,
        )
    }

    /**
     * First target scales with proven history: someone who has never gone a
     * week aims for 7 days; someone who has done a month aims for 30. An
     * achievable first win beats an intimidating one (plan §15).
     */
    private fun firstMilestone(previousStreak: PreviousStreak): Int = when (previousStreak) {
        PreviousStreak.NEVER_TRIED, PreviousStreak.DAYS_1_7 -> 7
        PreviousStreak.WEEKS_1_4 -> 14
        PreviousStreak.MONTHS_1_3, PreviousStreak.OVER_90_DAYS -> 30
    }

    /** The 2–3 personalized (non-foundation) categories, by weight. */
    private fun focusAreas(items: List<RecoveryPlanItem>): List<PlanCategory> =
        items.asSequence()
            .filter { it.category != PlanCategory.FOUNDATION }
            .groupBy { it.category }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<PlanCategory, List<RecoveryPlanItem>>> { it.value.size }
                    .thenBy { it.key.displayRank },
            )
            .take(MAX_FOCUS_AREAS)
            .map { it.key }

    private companion object {
        const val LOW_MOTIVATION_MAX = 4
        const val HIGH_MOTIVATION_MIN = 8
        const val MAX_FOCUS_AREAS = 3
    }
}
