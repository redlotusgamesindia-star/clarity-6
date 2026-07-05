package com.runtimelabs.clarity.feature.toolkit

/**
 * Pure breathing-session state machine: the ViewModel owns a 1 Hz clock and
 * calls [advanceOneSecond]; everything else — phase transitions, cycle
 * counting, elapsed time — is deterministic data, unit-tested without
 * coroutines or a real clock.
 */
enum class BreathPhaseKind { INHALE, HOLD_IN, EXHALE, HOLD_OUT }

data class BreathPhase(val kind: BreathPhaseKind, val seconds: Int) {
    init {
        require(seconds > 0)
    }
}

data class BreathingPattern(
    val code: String,
    val phases: List<BreathPhase>,
) {
    init {
        require(phases.isNotEmpty())
    }
}

/** The three shipped patterns. Calm (extended exhale) is the crisis default. */
object BreathingPatterns {
    val CALM = BreathingPattern(
        code = "calm_4_6",
        phases = listOf(
            BreathPhase(BreathPhaseKind.INHALE, 4),
            BreathPhase(BreathPhaseKind.EXHALE, 6),
        ),
    )
    val BOX = BreathingPattern(
        code = "box_4",
        phases = listOf(
            BreathPhase(BreathPhaseKind.INHALE, 4),
            BreathPhase(BreathPhaseKind.HOLD_IN, 4),
            BreathPhase(BreathPhaseKind.EXHALE, 4),
            BreathPhase(BreathPhaseKind.HOLD_OUT, 4),
        ),
    )
    val RELAX = BreathingPattern(
        code = "relax_4_7_8",
        phases = listOf(
            BreathPhase(BreathPhaseKind.INHALE, 4),
            BreathPhase(BreathPhaseKind.HOLD_IN, 7),
            BreathPhase(BreathPhaseKind.EXHALE, 8),
        ),
    )
    val ALL = listOf(CALM, BOX, RELAX)

    fun byCode(code: String): BreathingPattern = ALL.firstOrNull { it.code == code } ?: CALM
}

data class BreathingSessionState(
    val pattern: BreathingPattern,
    val phaseIndex: Int = 0,
    val secondsRemainingInPhase: Int = pattern.phases.first().seconds,
    val completedCycles: Int = 0,
    val elapsedSeconds: Int = 0,
) {
    val currentPhase: BreathPhase get() = pattern.phases[phaseIndex]
}

fun advanceOneSecond(state: BreathingSessionState): BreathingSessionState {
    val elapsed = state.elapsedSeconds + 1
    val remaining = state.secondsRemainingInPhase - 1
    if (remaining > 0) {
        return state.copy(secondsRemainingInPhase = remaining, elapsedSeconds = elapsed)
    }
    val nextIndex = (state.phaseIndex + 1) % state.pattern.phases.size
    val wrapped = nextIndex == 0
    return state.copy(
        phaseIndex = nextIndex,
        secondsRemainingInPhase = state.pattern.phases[nextIndex].seconds,
        completedCycles = state.completedCycles + if (wrapped) 1 else 0,
        elapsedSeconds = elapsed,
    )
}
