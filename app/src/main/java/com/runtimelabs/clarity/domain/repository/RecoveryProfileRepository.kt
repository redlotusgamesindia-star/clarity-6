package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.RecoveryPlan
import com.runtimelabs.clarity.domain.model.RecoveryProfile
import kotlinx.coroutines.flow.Flow

/**
 * The user's recovery profile and generated plan. Both are user data ->
 * stored in the encrypted Room database (never DataStore), per the
 * persistence-split rule in ARCHITECTURE.md §5.
 */
interface RecoveryProfileRepository {

    /** Null until onboarding has been completed once. */
    val profile: Flow<RecoveryProfile?>

    /** Null until a plan has been generated and saved. */
    val plan: Flow<RecoveryPlan?>

    /**
     * Atomically replaces profile + plan (single Room transaction).
     * Called at onboarding completion and by any future "retake assessment".
     */
    suspend fun saveProfileAndPlan(profile: RecoveryProfile, plan: RecoveryPlan)
}
