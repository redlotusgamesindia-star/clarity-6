package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.RelapseTrigger
import javax.inject.Inject

/**
 * Builds the short "what now" checklist shown at the end of the recovery
 * flow. Same contract as [com.runtimelabs.clarity.domain.plan.RecoveryPlanGenerator]:
 * pure, deterministic, every item traceable to a specific answer (or to
 * nothing, for the five universal foundations). The trigger input is
 * optional — a person who skipped that step still gets the five
 * foundations, since the checklist should never feel gated behind
 * disclosure.
 */
class RecoveryChecklistGenerator @Inject constructor() {

    fun generate(trigger: RelapseTrigger?): List<RecoveryChecklistItem> {
        val codes = LinkedHashSet<RecoveryChecklistItemCode>()

        // Five universal foundations — physiological and emotional resets
        // that apply regardless of what triggered this particular relapse.
        codes += RecoveryChecklistItemCode.DRINK_WATER
        codes += RecoveryChecklistItemCode.TAKE_WALK
        codes += RecoveryChecklistItemCode.SHOWER
        codes += RecoveryChecklistItemCode.BREATHING_EXERCISE
        codes += RecoveryChecklistItemCode.JOURNAL_IT

        // Personalized addition, only when the reflection offered enough
        // to personalize with. NIGHT and COULDNT_SLEEP both point at the
        // same wind-down item — both are the same underlying circumstance.
        when (trigger) {
            RelapseTrigger.LONELINESS -> codes += RecoveryChecklistItemCode.REACH_OUT
            RelapseTrigger.BOREDOM -> codes += RecoveryChecklistItemCode.PLAN_NEXT_HOUR
            RelapseTrigger.NIGHT, RelapseTrigger.COULDNT_SLEEP -> codes += RecoveryChecklistItemCode.WIND_DOWN
            RelapseTrigger.STRESS, RelapseTrigger.SOCIAL_MEDIA, RelapseTrigger.OTHER, null -> Unit
        }

        return codes.map { RecoveryChecklistItem(it) }
    }
}
