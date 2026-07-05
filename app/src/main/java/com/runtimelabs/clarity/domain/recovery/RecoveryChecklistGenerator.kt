package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.UrgeTime
import javax.inject.Inject

/**
 * Builds the short "what now" checklist shown at the end of the recovery
 * flow. Same contract as [com.runtimelabs.clarity.domain.plan.RecoveryPlanGenerator]:
 * pure, deterministic, every item traceable to a specific answer (or to
 * nothing, for the five universal foundations). Reflection inputs are all
 * optional — a person who skipped every field still gets the five
 * foundations, since the checklist should never feel gated behind
 * disclosure.
 */
class RecoveryChecklistGenerator @Inject constructor() {

    fun generate(trigger: MainTrigger?, timeOfDay: UrgeTime?): List<RecoveryChecklistItem> {
        val codes = LinkedHashSet<RecoveryChecklistItemCode>()

        // Five universal foundations — physiological and emotional resets
        // that apply regardless of what triggered this particular relapse.
        codes += RecoveryChecklistItemCode.DRINK_WATER
        codes += RecoveryChecklistItemCode.TAKE_WALK
        codes += RecoveryChecklistItemCode.SHOWER
        codes += RecoveryChecklistItemCode.BREATHING_EXERCISE
        codes += RecoveryChecklistItemCode.JOURNAL_IT

        // Personalized additions, only when the reflection offered enough
        // to personalize with.
        when (trigger) {
            MainTrigger.LONELINESS -> codes += RecoveryChecklistItemCode.REACH_OUT
            MainTrigger.BOREDOM -> codes += RecoveryChecklistItemCode.PLAN_NEXT_HOUR
            else -> Unit
        }
        if (timeOfDay == UrgeTime.LATE_NIGHT) {
            codes += RecoveryChecklistItemCode.WIND_DOWN
        }

        return codes.map { RecoveryChecklistItem(it) }
    }
}
