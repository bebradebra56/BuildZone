package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.data.db.entity.ProjectEntity
import com.buildzone.zonebu.data.db.entity.RoomEntity
import com.buildzone.zonebu.data.repository.MeasurementRepository
import com.buildzone.zonebu.data.repository.ProjectRepository
import com.buildzone.zonebu.data.repository.RoomRepository
import kotlinx.coroutines.flow.*

data class RoomStats(
    val room: RoomEntity,
    val measurements: List<MeasurementEntity>,
    val avgTemp: Float,
    val minTemp: Float,
    val maxTemp: Float,
    val avgHumidity: Float,
    val problemCount: Int
)

data class ProjectReport(
    val project: ProjectEntity,
    val roomStats: List<RoomStats>,
    val totalMeasurements: Int,
    val overallAvgTemp: Float,
    val overallAvgHumidity: Float
)

class ReportsViewModel(
    private val projectRepository: ProjectRepository,
    private val roomRepository: RoomRepository,
    private val measurementRepository: MeasurementRepository
) : ViewModel() {

    val projects: Flow<List<ProjectEntity>> = projectRepository.getAllProjects()

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId

    fun selectProject(id: Long) { _selectedProjectId.value = id }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getReportForProject(projectId: Long): Flow<ProjectReport?> =
        roomRepository.getRoomsForProject(projectId)
            .flatMapLatest { rooms ->
                if (rooms.isEmpty()) return@flatMapLatest flowOf(null)
                val project = projectRepository.getProjectById(projectId)
                val measurementFlows = rooms.map { room ->
                    measurementRepository.getMeasurementsForRoom(room.id)
                        .map { measurements -> Pair(room, measurements) }
                }
                combine(measurementFlows) { pairs ->
                    val roomStats = pairs.map { (room, measurements) ->
                        val temps = measurements.map { it.temperature }
                        val humidities = measurements.map { it.humidity }
                        val problems = measurements.count { it.temperature < 18f || it.temperature > 27f || it.humidity > 65f }
                        RoomStats(
                            room = room,
                            measurements = measurements,
                            avgTemp = temps.average().toFloat().takeIf { it.isFinite() } ?: 0f,
                            minTemp = temps.minOrNull() ?: 0f,
                            maxTemp = temps.maxOrNull() ?: 0f,
                            avgHumidity = humidities.average().toFloat().takeIf { it.isFinite() } ?: 0f,
                            problemCount = problems
                        )
                    }
                    Triple(project, roomStats, pairs.sumOf { it.second.size })
                }.flatMapLatest { (projectFlow, roomStats, total) ->
                    projectFlow.map { proj ->
                        proj?.let {
                            val allTemps = roomStats.flatMap { rs -> rs.measurements.map { it.temperature } }
                            val allHumidities = roomStats.flatMap { rs -> rs.measurements.map { it.humidity } }
                            ProjectReport(
                                project = it,
                                roomStats = roomStats,
                                totalMeasurements = total,
                                overallAvgTemp = allTemps.average().toFloat().takeIf { v -> v.isFinite() } ?: 0f,
                                overallAvgHumidity = allHumidities.average().toFloat().takeIf { v -> v.isFinite() } ?: 0f
                            )
                        }
                    }
                }
            }
}
