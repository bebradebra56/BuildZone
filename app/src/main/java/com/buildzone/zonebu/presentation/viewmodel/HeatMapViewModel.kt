package com.buildzone.zonebu.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.data.repository.MeasurementRepository
import com.buildzone.zonebu.ui.theme.humidityToColor
import com.buildzone.zonebu.ui.theme.temperatureToColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class HeatMapMode { TEMPERATURE, HUMIDITY }

class HeatMapViewModel(
    private val measurementRepository: MeasurementRepository,
    val roomId: Long
) : ViewModel() {

    val measurements: StateFlow<List<MeasurementEntity>> =
        measurementRepository.getMeasurementsForRoom(roomId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mode = MutableStateFlow(HeatMapMode.TEMPERATURE)
    val mode: StateFlow<HeatMapMode> = _mode

    private val _heatMapBitmap = MutableStateFlow<Bitmap?>(null)
    val heatMapBitmap: StateFlow<Bitmap?> = _heatMapBitmap

    init {
        viewModelScope.launch {
            combine(measurements, _mode) { m, mode -> Pair(m, mode) }
                .collectLatest { (m, mode) -> computeHeatMap(m, mode) }
        }
    }

    fun setMode(mode: HeatMapMode) { _mode.value = mode }

    private fun computeHeatMap(measurements: List<MeasurementEntity>, mode: HeatMapMode) {
        if (measurements.isEmpty()) {
            _heatMapBitmap.value = null
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val size = 120
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (py in 0 until size) {
                for (px in 0 until size) {
                    val xRatio = px.toFloat() / size
                    val yRatio = py.toFloat() / size
                    var weightedSum = 0f
                    var totalWeight = 0f
                    measurements.forEach { m ->
                        val dx = xRatio - m.xRatio
                        val dy = yRatio - m.yRatio
                        val distSq = dx * dx + dy * dy
                        if (distSq < 0.000001f) {
                            weightedSum = if (mode == HeatMapMode.TEMPERATURE) m.temperature else m.humidity
                            totalWeight = 1f
                            return@forEach
                        }
                        val weight = 1f / distSq
                        val value = if (mode == HeatMapMode.TEMPERATURE) m.temperature else m.humidity
                        weightedSum += weight * value
                        totalWeight += weight
                    }
                    val value = if (totalWeight > 0) weightedSum / totalWeight else 20f
                    val color = if (mode == HeatMapMode.TEMPERATURE) temperatureToColor(value) else humidityToColor(value)
                    bitmap.setPixel(px, py, android.graphics.Color.argb(
                        (color.alpha * 255).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    ))
                }
            }
            _heatMapBitmap.value = bitmap
        }
    }
}
