package com.buildzone.zonebu.data.repository

import com.buildzone.zonebu.data.db.dao.ActivityLogDao
import com.buildzone.zonebu.data.db.dao.RoomDao
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import com.buildzone.zonebu.data.db.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

class RoomRepository(
    private val roomDao: RoomDao,
    private val activityLogDao: ActivityLogDao
) {
    fun getRoomsForProject(projectId: Long): Flow<List<RoomEntity>> =
        roomDao.getRoomsForProject(projectId)

    fun getRoomById(id: Long): Flow<RoomEntity?> = roomDao.getRoomById(id)

    fun getRoomCountForProject(projectId: Long): Flow<Int> =
        roomDao.getRoomCountForProject(projectId)

    fun getTotalRoomCount(): Flow<Int> = roomDao.getTotalRoomCount()

    suspend fun createRoom(
        projectId: Long, name: String,
        widthMeters: Float, heightMeters: Float, description: String
    ): Long {
        val id = roomDao.insertRoom(
            RoomEntity(
                projectId = projectId, name = name,
                widthMeters = widthMeters, heightMeters = heightMeters,
                description = description
            )
        )
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "ROOM", entityId = id,
                action = "CREATED", description = "Room created: $name"
            )
        )
        return id
    }

    suspend fun updateRoom(room: RoomEntity) {
        roomDao.updateRoom(room)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "ROOM", entityId = room.id,
                action = "UPDATED", description = "Room updated: ${room.name}"
            )
        )
    }

    suspend fun deleteRoom(id: Long, name: String) {
        roomDao.deleteRoomById(id)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "ROOM", entityId = id,
                action = "DELETED", description = "Room deleted: $name"
            )
        )
    }
}
