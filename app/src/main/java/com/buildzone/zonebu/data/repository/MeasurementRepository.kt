package com.buildzone.zonebu.data.repository

import com.buildzone.zonebu.data.db.dao.ActivityLogDao
import com.buildzone.zonebu.data.db.dao.MeasurementDao
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(
    private val measurementDao: MeasurementDao,
    private val activityLogDao: ActivityLogDao
) {
    fun getMeasurementsForRoom(roomId: Long): Flow<List<MeasurementEntity>> =
        measurementDao.getMeasurementsForRoom(roomId)

    fun getMeasurementsForRoomByTime(roomId: Long): Flow<List<MeasurementEntity>> =
        measurementDao.getMeasurementsForRoomByTime(roomId)

    fun getMeasurementById(id: Long): Flow<MeasurementEntity?> =
        measurementDao.getMeasurementById(id)

    fun getRecentMeasurements(limit: Int = 10): Flow<List<MeasurementEntity>> =
        measurementDao.getRecentMeasurements(limit)

    fun getMeasurementCountForRoom(roomId: Long): Flow<Int> =
        measurementDao.getMeasurementCountForRoom(roomId)

    fun getTotalMeasurementCount(): Flow<Int> = measurementDao.getTotalMeasurementCount()

    fun getMeasurementsBetween(roomId: Long, start: Long, end: Long): Flow<List<MeasurementEntity>> =
        measurementDao.getMeasurementsBetween(roomId, start, end)

    suspend fun addMeasurement(
        roomId: Long, xRatio: Float, yRatio: Float,
        temperature: Float, humidity: Float, label: String, notes: String
    ): Long {
        val id = measurementDao.insertMeasurement(
            MeasurementEntity(
                roomId = roomId, xRatio = xRatio, yRatio = yRatio,
                temperature = temperature, humidity = humidity,
                label = label, notes = notes
            )
        )
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "MEASUREMENT", entityId = id,
                action = "CREATED",
                description = "Measurement added: ${temperature}°C, ${humidity}% humidity"
            )
        )
        return id
    }

    suspend fun updateMeasurement(measurement: MeasurementEntity) {
        measurementDao.updateMeasurement(measurement)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "MEASUREMENT", entityId = measurement.id,
                action = "UPDATED",
                description = "Measurement updated: ${measurement.temperature}°C, ${measurement.humidity}%"
            )
        )
    }

    suspend fun deleteMeasurement(id: Long) {
        measurementDao.deleteMeasurementById(id)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "MEASUREMENT", entityId = id,
                action = "DELETED", description = "Measurement removed"
            )
        )
    }
}
