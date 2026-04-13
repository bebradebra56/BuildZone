package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.data.repository.*
import kotlinx.coroutines.flow.Flow

class DashboardViewModel(
    private val projectRepository: ProjectRepository,
    private val roomRepository: RoomRepository,
    private val measurementRepository: MeasurementRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val projectCount: Flow<Int> = projectRepository.getProjectCount()
    val totalRoomCount: Flow<Int> = roomRepository.getTotalRoomCount()
    val totalMeasurementCount: Flow<Int> = measurementRepository.getTotalMeasurementCount()
    val recentMeasurements: Flow<List<MeasurementEntity>> = measurementRepository.getRecentMeasurements(8)
    val pendingTaskCount: Flow<Int> = taskRepository.getPendingTaskCount()
}
