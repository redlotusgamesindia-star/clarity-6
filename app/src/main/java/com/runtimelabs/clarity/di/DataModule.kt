package com.runtimelabs.clarity.di

import com.runtimelabs.clarity.data.repository.RecoveryProfileRepositoryImpl
import com.runtimelabs.clarity.data.repository.SettingsRepositoryImpl
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Interface -> implementation bindings. @Binds (not @Provides) so Dagger
 * generates zero wrapper code. One binding per repository as they arrive.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindRecoveryProfileRepository(impl: RecoveryProfileRepositoryImpl): RecoveryProfileRepository
}
