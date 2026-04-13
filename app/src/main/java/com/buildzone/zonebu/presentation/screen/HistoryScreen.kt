package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.presentation.components.*
import com.buildzone.zonebu.presentation.viewmodel.ActivityViewModel
import com.buildzone.zonebu.presentation.viewmodel.MeasurementViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    val viewModel: ActivityViewModel = koinViewModel()
    val logs by viewModel.recentLogs.collectAsState(emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "History",
                actions = {
                    IconButton(onClick = { navController.navigate("activity_history") }) {
                        Icon(Icons.Filled.History, contentDescription = "All History")
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "No activity yet",
                    subtitle = "Your activity history will appear here"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    ActivityLogItem(log)
                }
            }
        }
    }
}

@Composable
fun ActivityLogItem(log: ActivityLogEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.ENGLISH) }
    val (icon, color) = when (log.action) {
        "CREATED" -> Pair(Icons.Filled.Add, NeutralZone)
        "UPDATED" -> Pair(Icons.Filled.Edit, AccentBlue)
        "DELETED" -> Pair(Icons.Filled.Delete, ErrorRed)
        "COMPLETED" -> Pair(Icons.Filled.CheckCircle, NeutralZone)
        "REOPENED" -> Pair(Icons.Filled.Refresh, WarmZone)
        else -> Pair(Icons.Filled.Info, AccentIndigo)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                log.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivityHistoryScreen(navController: NavController) {
    val viewModel: ActivityViewModel = koinViewModel()
    val logs by viewModel.logs.collectAsState(emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Activity History",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All")
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(icon = Icons.Filled.History, title = "No history", subtitle = "Actions will be recorded here")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    ActivityLogItem(log)
                    HorizontalDivider(modifier = Modifier.padding(start = 44.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
fun TimelineScreen(navController: NavController, roomId: Long) {
    val viewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val measurements by viewModel.measurementsByTime.collectAsState(emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "Timeline", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (measurements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.Timeline, title = "No data", subtitle = "Add measurements to see the timeline")
                }
            } else {
                Text(
                    "Temperature & Humidity Over Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )

                TimelineChart(
                    measurements = measurements,
                    isCelsius = isCelsius,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(measurements.reversed(), key = { it.id }) { m ->
                        val fmt = SimpleDateFormat("MMM dd, HH:mm", Locale.ENGLISH)
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    fmt.format(Date(m.timestamp)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TemperatureChip(m.temperature, isCelsius)
                                    HumidityChip(m.humidity)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineChart(
    measurements: List<MeasurementEntity>,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    if (measurements.size < 2) return

    val tempColor = AccentBlue
    val humidityColor = AccentCyan

    Canvas(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))) {
        val paddingPx = 40f
        val chartW = size.width - paddingPx * 2
        val chartH = size.height - paddingPx * 2

        val minTemp = measurements.minOf { it.temperature }
        val maxTemp = measurements.maxOf { it.temperature }
        val tempRange = (maxTemp - minTemp).coerceAtLeast(5f)

        val minTime = measurements.first().timestamp.toFloat()
        val maxTime = measurements.last().timestamp.toFloat()
        val timeRange = (maxTime - minTime).coerceAtLeast(1f)

        fun tempToY(temp: Float): Float {
            return paddingPx + chartH - ((temp - minTemp) / tempRange * chartH)
        }

        fun humToY(hum: Float): Float {
            return paddingPx + chartH - (hum / 100f * chartH)
        }

        fun timeToX(time: Long): Float {
            return paddingPx + ((time - minTime) / timeRange * chartW)
        }

        val gridColor = Color(0xFF94A3B8).copy(alpha = 0.3f)
        for (i in 0..4) {
            val y = paddingPx + chartH / 4 * i
            drawLine(gridColor, Offset(paddingPx, y), Offset(paddingPx + chartW, y), strokeWidth = 1f)
        }

        val tempPath = Path()
        measurements.forEachIndexed { index, m ->
            val x = timeToX(m.timestamp)
            val y = tempToY(if (isCelsius) m.temperature else m.temperature * 9f / 5f + 32f)
            if (index == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
        }
        drawPath(tempPath, color = tempColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

        val humPath = Path()
        measurements.forEachIndexed { index, m ->
            val x = timeToX(m.timestamp)
            val y = humToY(m.humidity)
            if (index == 0) humPath.moveTo(x, y) else humPath.lineTo(x, y)
        }
        drawPath(humPath, color = humidityColor, style = Stroke(width = 2f, cap = StrokeCap.Round))

        measurements.forEach { m ->
            drawCircle(tempColor, 5f, Offset(timeToX(m.timestamp), tempToY(if (isCelsius) m.temperature else m.temperature * 9f / 5f + 32f)))
            drawCircle(humidityColor, 4f, Offset(timeToX(m.timestamp), humToY(m.humidity)))
        }
    }

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(12.dp, 3.dp).background(tempColor, CircleShape))
            Text("Temperature", style = MaterialTheme.typography.labelSmall, color = tempColor)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(12.dp, 3.dp).background(humidityColor, CircleShape))
            Text("Humidity", style = MaterialTheme.typography.labelSmall, color = humidityColor)
        }
    }
}
