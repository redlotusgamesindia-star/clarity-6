package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.StreakSnapshot
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * "Instead of only tracking streaks" (the whole point of this phase):
 * blends lifetime consistency with current momentum and proven capability,
 * so one relapse — or twelve — never collapses the number to something that
 * reads as failure. The weighting is deliberate: clean rate (lifetime
 * consistency) dominates because it's the truest long-run signal and the
 * hardest to fake with a single good week; momentum and capability are
 * capped bonuses, not requirements, specifically so a person with a strong
 * history who just restarted still sees a score that reflects that history
 * rather than punishing them for being on day one again.
 *
 * Pure and deterministic (AD-4): every component is a plain function of the
 * already-computed [StreakSnapshot], nothing here reads a clock or stores
 * anything — the score is recomputed fresh every time it's shown, exactly
 * like the streak itself.
 */
class RecoveryScoreCalculator @Inject constructor() {

    fun compute(snapshot: StreakSnapshot): RecoveryScore {
        val totalDays = snapshot.totalCleanDays + snapshot.totalRelapses
        val cleanRate = if (totalDays <= 0) 1f else snapshot.totalCleanDays / totalDays.toFloat()
        val momentumCredit = (snapshot.currentDays / MOMENTUM_CAP_DAYS).coerceIn(0f, 1f)
        val capabilityCredit = (snapshot.longestDays / CAPABILITY_CAP_DAYS).coerceIn(0f, 1f)

        val percent = (
            cleanRate * CLEAN_RATE_WEIGHT +
                momentumCredit * MOMENTUM_WEIGHT +
                capabilityCredit * CAPABILITY_WEIGHT
            ).roundToInt().coerceIn(0, 100)

        return RecoveryScore(
            percent = percent,
            totalCleanDays = snapshot.totalCleanDays,
            totalRelapses = snapshot.totalRelapses,
            bestStreakDays = snapshot.longestDays,
            currentStreakDays = snapshot.currentDays,
        )
    }

    private companion object {
        const val CLEAN_RATE_WEIGHT = 60f
        const val MOMENTUM_WEIGHT = 20f
        const val CAPABILITY_WEIGHT = 20f
        const val MOMENTUM_CAP_DAYS = 30f
        const val CAPABILITY_CAP_DAYS = 90f // mirrors the 90-day reboot framing used since onboarding
    }
}
