package com.buildzone.zonebu.data.db.dao

import androidx.room.*
import com.buildzone.zonebu.data.db.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getRoomsForProject(projectId: Long): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getRoomById(id: Long): Flow<RoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun deleteRoomById(id: Long)

    @Query("SELECT COUNT(*) FROM rooms WHERE projectId = :projectId")
    fun getRoomCountForProject(projectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM rooms")
    fun getTotalRoomCount(): Flow<Int>
}
