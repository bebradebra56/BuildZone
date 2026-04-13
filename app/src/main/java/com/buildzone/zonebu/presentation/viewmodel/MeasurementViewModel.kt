package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeasurementViewModel(
    private val measurementRepository: MeasurementRepository,
    val roomId: Long
) : ViewModel() {

    val measurements: Flow<List<MeasurementEntity>> =
        measurementRepository.getMeasurementsForRoom(roomId)

    val measurementsByTime: Flow<List<MeasurementEntity>> =
        measurementRepository.getMeasurementsForRoomByTime(roomId)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun addMeasurement(
        xRatio: Float, yRatio: Float,
        temperature: Float, humidity: Float,
        label: String, notes: String
    ) {
        viewModelScope.launch {
            measurementRepository.addMeasurement(roomId, xRatio, yRatio, temperature, humidity, label, notes)
            _saved.value = true
        }
    }

    fun updateMeasurement(measurement: MeasurementEntity) {
        viewModelScope.launch { measurementRepository.updateMeasurement(measurement) }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch { measurementRepository.deleteMeasurement(id) }
    }

    fun getMeasurementById(id: Long): Flow<MeasurementEntity?> =
        measurementRepository.getMeasurementById(id)

    fun clearSaved() { _saved.value = false }
}
