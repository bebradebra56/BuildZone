package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.presentation.components.*
import com.buildzone.zonebu.presentation.viewmodel.MeasurementViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.temperatureToColor
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MeasurementsScreen(navController: NavController, roomId: Long) {
    val viewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val measurements by viewModel.measurements.collectAsState(emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Measurements",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("timeline/$roomId") }) {
                        Icon(Icons.Filled.Timeline, contentDescription = "Timeline")
                    }
                }
            )
        }
    ) { padding ->
        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.Thermostat,
                    title = "No measurements",
                    subtitle = "Add measurement points on the floor plan"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val avgTemp = measurements.map { it.temperature }.average().toFloat()
                    val avgHum = measurements.map { it.humidity }.average().toFloat()
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatMini("Points", measurements.size.toString(), Icons.Filled.LocationOn)
                            StatMini(
                                "Avg Temp",
                                if (isCelsius) "%.1f°C".format(avgTemp) else "%.1f°F".format(avgTemp * 9f / 5f + 32f),
                                Icons.Filled.Thermostat
                            )
                            StatMini("Avg Humidity", "%.0f%%".format(avgHum), Icons.Filled.WaterDrop)
                        }
                    }
                }

                items(measurements, key = { it.id }) { m ->
                    MeasurementDetailCard(
                        measurement = m,
                        isCelsius = isCelsius,
                        onEdit = { navController.navigate("edit_measurement/${m.id}") },
                        onDelete = { viewModel.deleteMeasurement(m.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMini(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MeasurementDetailCard(
    measurement: MeasurementEntity,
    isCelsius: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.ENGLISH) }
    var showMenu by remember { mutableStateOf(false) }
    val tempColor = temperatureToColor(measurement.temperature)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZoneColorDot(measurement.temperature, 12.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    measurement.label.ifEmpty { "Point (%.0f%%, %.0f%%)".format(measurement.xRatio * 100, measurement.yRatio * 100) },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.MoreVert, null, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Filled.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TemperatureChip(measurement.temperature, isCelsius)
                HumidityChip(measurement.humidity)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                dateFormat.format(Date(measurement.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (measurement.notes.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    measurement.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
