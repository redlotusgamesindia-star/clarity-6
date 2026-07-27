package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.CheckInDao
import com.runtimelabs.clarity.data.local.db.entity.DailyCheckInEntity
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.repository.CheckInRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val dao: CheckInDao,
) : CheckInRepository {

    override fun observeSince(sinceEpochDay: Long): Flow<List<DailyCheckIn>> =
        dao.observeSince(sinceEpochDay)
            .map { rows ->
                rows.map { row ->
                    DailyCheckIn(
                        epochDay = row.epochDay,
                        mood = MoodLevel.fromStorageValue(row.mood),
                        urgeLevel = row.urgeLevel.coerceIn(0, 10),
                        updatedAtEpochMillis = row.updatedAtEpochMillis,
                    )
                }
            }
            .distinctUntilChanged()

    override suspend fun upsert(checkIn: DailyCheckIn) {
        dao.upsert(
            DailyCheckInEntity(
                epochDay = checkIn.epochDay,
                mood = checkIn.mood.storageValue,
                urgeLevel = checkIn.urgeLevel,
                updatedAtEpochMillis = checkIn.updatedAtEpochMillis,
            ),
        )
    }
}
