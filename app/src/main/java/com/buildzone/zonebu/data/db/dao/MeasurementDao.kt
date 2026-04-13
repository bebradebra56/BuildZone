package com.buildzone.zonebu.data.db.dao

import androidx.room.*
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE roomId = :roomId ORDER BY timestamp DESC")
    fun getMeasurementsForRoom(roomId: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMeasurementsForRoomByTime(roomId: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    fun getMeasurementById(id: Long): Flow<MeasurementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMeasurements(limit: Int = 10): Flow<List<MeasurementEntity>>

    @Query("SELECT COUNT(*) FROM measurements WHERE roomId = :roomId")
    fun getMeasurementCountForRoom(roomId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM measurements")
    fun getTotalMeasurementCount(): Flow<Int>

    @Query("SELECT * FROM measurements WHERE roomId = :roomId AND timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun getMeasurementsBetween(roomId: Long, start: Long, end: Long): Flow<List<MeasurementEntity>>
}
