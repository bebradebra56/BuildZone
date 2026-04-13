package com.buildzone.zonebu.data.repository

import com.buildzone.zonebu.data.db.dao.ActivityLogDao
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val activityLogDao: ActivityLogDao) {
    fun getAllLogs(): Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()

    fun getRecentLogs(limit: Int = 20): Flow<List<ActivityLogEntity>> =
        activityLogDao.getRecentLogs(limit)

    suspend fun clearOldLogs(beforeMillis: Long) {
        activityLogDao.deleteLogsOlderThan(beforeMillis)
    }

    suspend fun clearAll() {
        activityLogDao.clearAll()
    }
}
