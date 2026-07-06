package com.runtimelabs.clarity.domain.insight

import com.runtimelabs.clarity.domain.habit.DayStat
import com.runtimelabs.clarity.domain.habit.HabitRate
import com.runtimelabs.clarity.domain.habit.HabitWindowStats
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.model.StreakSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightGeneratorTest {

    private val generator = InsightGenerator()

    private fun stats(scheduled: Int, completed: Int, perHabit: List<HabitRate> = emptyList()) =
        HabitWindowStats(
            days = listOf(DayStat(epochDay = 0, scheduled = scheduled, completed = completed)),
            perHabit = perHabit,
        )

    private fun checkIn(day: Long, mood: MoodLevel = MoodLevel.OKAY, urge: Int = 5) =
        DailyCheckIn(epochDay = day, mood = mood, urgeLevel = urge, updatedAtEpochMillis = 0)

    // None of these tests are relapse-related, so the four fields added to
    // StreakSnapshot after this file was first written (previousRunDays,
    // bestClosedRunDays, totalRelapses, totalCleanDays — see ARCHITECTURE.md
    // §22) get simple, internally-consistent defaults here rather than being
    // threaded through every call site.
    private fun streakSnapshot(currentDays: Int, longestDays: Int, cleanSinceEpochDay: Long) = StreakSnapshot(
        currentDays = currentDays,
        longestDays = longestDays,
        cleanSinceEpochDay = cleanSinceEpochDay,
        previousRunDays = null,
        bestClosedRunDays = 0,
        totalRelapses = 0,
        totalCleanDays = currentDays,
    )

    private fun generate(
        thisWeek: HabitWindowStats = stats(0, 0),
        lastWeek: HabitWindowStats = stats(0, 0),
        checkInsThis: List<DailyCheckIn> = emptyList(),
        checkInsLast: List<DailyCheckIn> = emptyList(),
        streak: StreakSnapshot = streakSnapshot(1, 1, 0),
        milestone: Int = 7,
        today: Long = 100,
    ) = generator.generate(thisWeek, lastWeek, checkInsThis, checkInsLast, streak, milestone, today)

    private fun codes(insights: List<Insight>) = insights.map { it.code }

    @Test
    fun `perfect week fires and suppresses best habit`() {
        val result = generate(
            thisWeek = stats(6, 6, perHabit = listOf(HabitRate(1, "Walk", 6, 6))),
        )
        assertTrue(InsightCode.PERFECT_WEEK in codes(result))
        assertTrue(InsightCode.BEST_HABIT !in codes(result))
    }

    @Test
    fun `milestone near carries days remaining`() {
        val result = generate(streak = streakSnapshot(5, 5, 0), milestone = 7)
        val insight = result.first { it.code == InsightCode.MILESTONE_NEAR }
        assertEquals(2, insight.value)
    }

    @Test
    fun `milestone already reached does not fire`() {
        val result = generate(streak = streakSnapshot(9, 9, 0), milestone = 7)
        assertTrue(InsightCode.MILESTONE_NEAR !in codes(result))
    }

    @Test
    fun `consistency up needs fifteen points and both sample sizes`() {
        val up = generate(thisWeek = stats(10, 8), lastWeek = stats(10, 6))
        assertTrue(InsightCode.CONSISTENCY_UP in codes(up))
        assertEquals(20, up.first { it.code == InsightCode.CONSISTENCY_UP }.value)

        val smallDelta = generate(thisWeek = stats(10, 7), lastWeek = stats(10, 6))
        assertTrue(InsightCode.CONSISTENCY_UP !in codes(smallDelta))

        val tinySample = generate(thisWeek = stats(2, 2), lastWeek = stats(2, 0))
        assertTrue(InsightCode.CONSISTENCY_UP !in codes(tinySample))
    }

    @Test
    fun `consistency down is reported without a number`() {
        val result = generate(thisWeek = stats(10, 4), lastWeek = stats(10, 8))
        assertTrue(InsightCode.CONSISTENCY_DOWN in codes(result))
    }

    @Test
    fun `best and focus habit thresholds`() {
        val result = generate(
            thisWeek = stats(
                scheduled = 3, completed = 2,
                perHabit = listOf(
                    HabitRate(1, "Walk", scheduled = 5, completed = 4),   // 80% -> best
                    HabitRate(2, "Read", scheduled = 5, completed = 1),   // 20% -> focus
                    HabitRate(3, "New", scheduled = 2, completed = 0),    // too few -> ignored
                ),
            ),
        )
        val best = result.first { it.code == InsightCode.BEST_HABIT }
        assertEquals("Walk", best.habitName)
        assertEquals(80, best.value)
        assertEquals("Read", result.first { it.code == InsightCode.FOCUS_HABIT }.habitName)
    }

    @Test
    fun `one habit cannot be both best and focus`() {
        val result = generate(
            thisWeek = stats(3, 2, perHabit = listOf(HabitRate(1, "Walk", 5, 2))), // 40%
        )
        // 40% is below focus threshold but it's also the max — best doesn't fire (<60),
        // focus may; never both for the same name.
        val bestNames = result.filter { it.code == InsightCode.BEST_HABIT }.map { it.habitName }
        val focusNames = result.filter { it.code == InsightCode.FOCUS_HABIT }.map { it.habitName }
        assertTrue(bestNames.intersect(focusNames.toSet()).isEmpty())
    }

    @Test
    fun `mood trending up needs half a point and three check-ins each week`() {
        val thisWeek = listOf(
            checkIn(98, MoodLevel.GOOD), checkIn(99, MoodLevel.GOOD), checkIn(100, MoodLevel.GREAT),
        )
        val lastWeek = listOf(
            checkIn(91, MoodLevel.OKAY), checkIn(92, MoodLevel.OKAY), checkIn(93, MoodLevel.OKAY),
        )
        val result = generate(checkInsThis = thisWeek, checkInsLast = lastWeek)
        assertTrue(InsightCode.MOOD_TRENDING_UP in codes(result))

        val tooFew = generate(checkInsThis = thisWeek.take(2), checkInsLast = lastWeek)
        assertTrue(InsightCode.MOOD_TRENDING_UP !in codes(tooFew))
    }

    @Test
    fun `urges easing needs a full point drop`() {
        val thisWeek = listOf(checkIn(98, urge = 2), checkIn(99, urge = 3), checkIn(100, urge = 2))
        val lastWeek = listOf(checkIn(91, urge = 5), checkIn(92, urge = 5), checkIn(93, urge = 4))
        val result = generate(checkInsThis = thisWeek, checkInsLast = lastWeek)
        assertTrue(InsightCode.URGES_EASING in codes(result))
    }

    @Test
    fun `check-in streak counts consecutive days ending today or yesterday`() {
        val days = listOf(checkIn(98), checkIn(99), checkIn(100))
        val result = generate(checkInsThis = days, today = 100)
        assertEquals(3, result.first { it.code == InsightCode.CHECKIN_STREAK }.value)

        val endsYesterday = generate(checkInsThis = listOf(checkIn(97), checkIn(98), checkIn(99)), today = 100)
        assertEquals(3, endsYesterday.first { it.code == InsightCode.CHECKIN_STREAK }.value)

        val gapped = generate(checkInsThis = listOf(checkIn(96), checkIn(97), checkIn(98)), today = 100)
        assertTrue(InsightCode.CHECKIN_STREAK !in codes(gapped))
    }

    @Test
    fun `empty data falls back to getting started`() {
        val result = generate()
        assertEquals(listOf(InsightCode.GETTING_STARTED), codes(result))
    }

    @Test
    fun `caps at three by priority order`() {
        val result = generate(
            thisWeek = stats(6, 6),
            lastWeek = stats(10, 5),
            checkInsThis = listOf(checkIn(98), checkIn(99), checkIn(100)),
            streak = streakSnapshot(5, 5, 0),
            milestone = 7,
            today = 100,
        )
        assertEquals(3, result.size)
        assertEquals(
            listOf(InsightCode.PERFECT_WEEK, InsightCode.MILESTONE_NEAR, InsightCode.CONSISTENCY_UP),
            codes(result),
        )
    }

    @Test
    fun `deterministic for identical inputs`() {
        val a = generate(thisWeek = stats(6, 5), lastWeek = stats(6, 2))
        val b = generate(thisWeek = stats(6, 5), lastWeek = stats(6, 2))
        assertEquals(a, b)
    }
}
