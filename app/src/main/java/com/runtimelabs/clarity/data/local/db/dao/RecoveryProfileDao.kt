package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.runtimelabs.clarity.data.local.db.entity.RecoveryPlanItemEntity
import com.runtimelabs.clarity.data.local.db.entity.RecoveryProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecoveryProfileDao {

    @Query("SELECT * FROM recovery_profile WHERE id = ${RecoveryProfileEntity.SINGLETON_ID}")
    fun observeProfile(): Flow<RecoveryProfileEntity?>

    @Query("SELECT * FROM recovery_plan_item ORDER BY orderIndex ASC")
    fun observePlanItems(): Flow<List<RecoveryPlanItemEntity>>

    @Upsert
    suspend fun upsertProfile(profile: RecoveryProfileEntity)

    @Query("DELETE FROM recovery_plan_item")
    suspend fun clearPlanItems()

    @Insert
    suspend fun insertPlanItems(items: List<RecoveryPlanItemEntity>)

    /**
     * Profile and plan are only ever written together; a crash between the
     * two must not be observable. Room wraps this default method in one
     * transaction.
     */
    @Transaction
    suspend fun replaceProfileAndPlan(
        profile: RecoveryProfileEntity,
        items: List<RecoveryPlanItemEntity>,
    ) {
        upsertProfile(profile)
        clearPlanItems()
        insertPlanItems(items)
    }
}
