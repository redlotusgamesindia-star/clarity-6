package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.RelapseReflection

interface RelapseReflectionRepository {
    suspend fun save(reflection: RelapseReflection)
}
