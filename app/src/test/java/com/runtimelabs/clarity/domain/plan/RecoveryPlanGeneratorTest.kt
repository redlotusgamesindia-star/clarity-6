package com.runtimelabs.clarity.domain.plan

import com.runtimelabs.clarity.domain.model.AgeRange
import com.runtimelabs.clarity.domain.model.GenderIdentity
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.PlanCategory
import com.runtimelabs.clarity.domain.model.PlanItemCode
import com.runtimelabs.clarity.domain.model.PreviousStreak
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.model.RecoveryProfile
import com.runtimelabs.clarity.domain.model.SleepSchedule
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.model.UsageFrequency
import com.runtimelabs.clarity.domain.model.YearsAddicted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The generator's contract is determinism + explainability: every rule below
 * pins one answer -> plan mapping so a copy-tweak refactor can't silently
 * change what users receive.
 */
class RecoveryPlanGeneratorTest {

    private val generator = RecoveryPlanGenerator()

    private fun profile(
        frequency: UsageFrequency = UsageFrequency.WEEKLY,
        trigger: MainTrigger = MainTrigger.BOREDOM,
        goal: RecoveryGoal = RecoveryGoal.QUIT_COMPLETELY,
        motivation: Int = 6,
        previousStreak: PreviousStreak = PreviousStreak.DAYS_1_7,
        urgeTime: UrgeTime = UrgeTime.EVENING,
        sleep: SleepSchedule = SleepSchedule.REGULAR,
    ) = RecoveryProfile(
        ageRange = AgeRange.AGE_25_34,
        gender = GenderIdentity.PREFER_NOT_TO_SAY,
        yearsAddicted = YearsAddicted.ONE_TO_THREE,
        frequency = frequency,
        mainTrigger = trigger,
        goal = goal,
        motivationLevel = motivation,
        reasonsToQuit = listOf(ReasonToQuit.SELF_RESPECT),
        previousStreak = previousStreak,
        strongestUrgeTime = urgeTime,
        sleepSchedule = sleep,
        createdAtEpochMillis = 0L,
    )

    private fun codes(profile: RecoveryProfile): Set<PlanItemCode> =
        generator.generate(profile).items.map { it.code }.toSet()

    @Test
    fun `foundation anchors are always present`() {
        val codes = codes(profile())
        assertTrue(PlanItemCode.DAILY_CHECKIN in codes)
        assertTrue(PlanItemCode.LEARN_URGE_TOOLKIT in codes)
        assertTrue(PlanItemCode.DEFINE_YOUR_WHY in codes)
        assertTrue(PlanItemCode.FIRST_MILESTONE_FOCUS in codes)
    }

    @Test
    fun `daily use adds risk mapping and device friction`() {
        val daily = codes(profile(frequency = UsageFrequency.DAILY))
        assertTrue(PlanItemCode.HIGH_RISK_MOMENT_MAP in daily)
        assertTrue(PlanItemCode.DEVICE_FRICTION_SETUP in daily)

        val occasional = codes(profile(frequency = UsageFrequency.FEW_PER_MONTH))
        assertFalse(PlanItemCode.HIGH_RISK_MOMENT_MAP in occasional)
        assertFalse(PlanItemCode.DEVICE_FRICTION_SETUP in occasional)
    }

    @Test
    fun `stress trigger adds the breathing reset`() {
        assertTrue(PlanItemCode.STRESS_RESET_BREATHING in codes(profile(trigger = MainTrigger.STRESS)))
    }

    @Test
    fun `late night urges add curfew and wind down`() {
        val codes = codes(profile(urgeTime = UrgeTime.LATE_NIGHT))
        assertTrue(PlanItemCode.LATE_NIGHT_DEVICE_CURFEW in codes)
        assertTrue(PlanItemCode.WIND_DOWN_ROUTINE in codes)
    }

    @Test
    fun `night owls get a sleep anchor`() {
        assertTrue(PlanItemCode.SLEEP_ANCHOR_TIME in codes(profile(sleep = SleepSchedule.NIGHT_OWL)))
        assertFalse(PlanItemCode.SLEEP_ANCHOR_TIME in codes(profile(sleep = SleepSchedule.REGULAR)))
    }

    @Test
    fun `motivation calibration matches level`() {
        assertTrue(PlanItemCode.START_TINY_COMMITMENT in codes(profile(motivation = 3)))
        assertTrue(PlanItemCode.CHANNEL_MOMENTUM in codes(profile(motivation = 9)))
        val mid = codes(profile(motivation = 6))
        assertFalse(PlanItemCode.START_TINY_COMMITMENT in mid)
        assertFalse(PlanItemCode.CHANNEL_MOMENTUM in mid)
    }

    @Test
    fun `goal maps to its framing item`() {
        assertTrue(PlanItemCode.ABSTINENCE_IDENTITY in codes(profile(goal = RecoveryGoal.QUIT_COMPLETELY)))
        assertTrue(PlanItemCode.REBOOT_90_FRAME in codes(profile(goal = RecoveryGoal.REBOOT_90_DAYS)))
        assertTrue(PlanItemCode.REDUCTION_LADDER in codes(profile(goal = RecoveryGoal.REDUCE_GRADUALLY)))
    }

    @Test
    fun `first milestone scales with proven history`() {
        assertEquals(7, generator.generate(profile(previousStreak = PreviousStreak.NEVER_TRIED)).firstMilestoneDays)
        assertEquals(7, generator.generate(profile(previousStreak = PreviousStreak.DAYS_1_7)).firstMilestoneDays)
        assertEquals(14, generator.generate(profile(previousStreak = PreviousStreak.WEEKS_1_4)).firstMilestoneDays)
        assertEquals(30, generator.generate(profile(previousStreak = PreviousStreak.MONTHS_1_3)).firstMilestoneDays)
        assertEquals(30, generator.generate(profile(previousStreak = PreviousStreak.OVER_90_DAYS)).firstMilestoneDays)
    }

    @Test
    fun `long past streaks add pattern review`() {
        assertTrue(PlanItemCode.STREAK_PATTERN_REVIEW in codes(profile(previousStreak = PreviousStreak.OVER_90_DAYS)))
        assertFalse(PlanItemCode.STREAK_PATTERN_REVIEW in codes(profile(previousStreak = PreviousStreak.DAYS_1_7)))
    }

    @Test
    fun `same profile always yields an identical plan`() {
        val p = profile(frequency = UsageFrequency.MULTIPLE_DAILY, trigger = MainTrigger.FATIGUE)
        assertEquals(generator.generate(p), generator.generate(p))
    }

    @Test
    fun `codes are unique and order indices contiguous`() {
        val plan = generator.generate(
            profile(
                frequency = UsageFrequency.MULTIPLE_DAILY,
                trigger = MainTrigger.FATIGUE,
                urgeTime = UrgeTime.LATE_NIGHT,
                sleep = SleepSchedule.IRREGULAR,
                motivation = 2,
                previousStreak = PreviousStreak.OVER_90_DAYS,
            ),
        )
        val codes = plan.items.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
        assertEquals(plan.items.indices.toList(), plan.items.map { it.orderIndex })
    }

    @Test
    fun `items are grouped by category display rank`() {
        val plan = generator.generate(profile(frequency = UsageFrequency.DAILY, sleep = SleepSchedule.NIGHT_OWL))
        val ranks = plan.items.map { it.category.displayRank }
        assertEquals(ranks.sorted(), ranks)
    }

    @Test
    fun `focus areas exclude foundation and cap at three`() {
        val plan = generator.generate(
            profile(
                frequency = UsageFrequency.MULTIPLE_DAILY,
                trigger = MainTrigger.FATIGUE,
                urgeTime = UrgeTime.LATE_NIGHT,
                sleep = SleepSchedule.IRREGULAR,
                motivation = 9,
            ),
        )
        assertFalse(PlanCategory.FOUNDATION in plan.focusAreas)
        assertTrue(plan.focusAreas.size <= 3)
        assertTrue(plan.focusAreas.isNotEmpty())
    }
}
