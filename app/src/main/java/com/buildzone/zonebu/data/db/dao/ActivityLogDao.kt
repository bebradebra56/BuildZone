package com.buildzone.zonebu.data.db.dao

import androidx.room.*
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 20): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE timestamp < :before")
    suspend fun deleteLogsOlderThan(before: Long)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAll()
}
