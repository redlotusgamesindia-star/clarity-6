package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.datastore.PremiumPreferences
import com.runtimelabs.clarity.domain.premium.PremiumRepository
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.domain.premium.isPremium
import com.runtimelabs.clarity.domain.premium.premiumStateOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val preferences: PremiumPreferences,
) : PremiumRepository {

    override val premiumState: Flow<PremiumState> = preferences.isPremium.map(::premiumStateOf)

    override suspend fun setPremiumState(state: PremiumState) =
        preferences.setIsPremium(state.isPremium)
}
