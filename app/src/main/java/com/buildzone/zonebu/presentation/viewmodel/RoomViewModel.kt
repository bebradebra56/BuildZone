package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.db.entity.RoomEntity
import com.buildzone.zonebu.data.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val roomRepository: RoomRepository,
    val projectId: Long
) : ViewModel() {

    val rooms: Flow<List<RoomEntity>> = roomRepository.getRoomsForProject(projectId)

    private val _newRoomId = MutableStateFlow<Long?>(null)
    val newRoomId: StateFlow<Long?> = _newRoomId

    fun createRoom(name: String, widthMeters: Float, heightMeters: Float, description: String) {
        viewModelScope.launch {
            val id = roomRepository.createRoom(projectId, name, widthMeters, heightMeters, description)
            _newRoomId.value = id
        }
    }

    fun updateRoom(room: RoomEntity) {
        viewModelScope.launch { roomRepository.updateRoom(room) }
    }

    fun deleteRoom(id: Long, name: String) {
        viewModelScope.launch { roomRepository.deleteRoom(id, name) }
    }

    fun getRoomById(id: Long): Flow<RoomEntity?> = roomRepository.getRoomById(id)

    fun clearNewRoomId() { _newRoomId.value = null }
}
