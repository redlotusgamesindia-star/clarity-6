package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.runtimelabs.clarity.data.local.db.entity.BadgeUnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeUnlockDao {

    @Query("SELECT * FROM badge_unlock ORDER BY unlockedAtEpochMillis ASC")
    fun observeAll(): Flow<List<BadgeUnlockEntity>>

    @Query("SELECT badge FROM badge_unlock")
    suspend fun getUnlockedCodes(): List<String>

    /**
     * Conflict-ignored: [com.runtimelabs.clarity.domain.repository.BadgeRepository.evaluateAndUnlock]
     * can be called from several sites in quick succession (app start, a
     * check-in save, a relapse confirm) without a race turning one real
     * unlock into a duplicate-feeling "unlock" event. Returns -1 when the
     * row already existed and nothing was inserted — the caller uses that
     * to know which badges were genuinely new just now, versus already
     * earned before this call.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BadgeUnlockEntity): Long
}
