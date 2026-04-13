package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.buildzone.zonebu.presentation.components.AppTopBar
import com.buildzone.zonebu.presentation.viewmodel.HeatMapMode
import com.buildzone.zonebu.presentation.viewmodel.HeatMapViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HeatMapScreen(navController: NavController, roomId: Long) {
    val viewModel: HeatMapViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val bitmap by viewModel.heatMapBitmap.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Heat Map",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("problem_zones/$roomId") }) {
                        Icon(Icons.Filled.Warning, contentDescription = "Problems")
                    }
                    IconButton(onClick = { navController.navigate("comparison/$roomId") }) {
                        Icon(Icons.Filled.CompareArrows, contentDescription = "Compare")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == HeatMapMode.TEMPERATURE,
                    onClick = { viewModel.setMode(HeatMapMode.TEMPERATURE) },
                    label = { Text("Temperature") },
                    leadingIcon = { Icon(Icons.Filled.Thermostat, null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = mode == HeatMapMode.HUMIDITY,
                    onClick = { viewModel.setMode(HeatMapMode.HUMIDITY) },
                    label = { Text("Humidity") },
                    leadingIcon = { Icon(Icons.Filled.WaterDrop, null, modifier = Modifier.size(16.dp)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (measurements.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Thermostat,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            "No measurements yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Add measurements on the floor plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    bitmap?.let { bmp ->
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            val imageBitmap = bmp.asImageBitmap()
                            drawImage(
                                image = imageBitmap,
                                dstSize = androidx.compose.ui.unit.IntSize(
                                    size.width.toInt(), size.height.toInt()
                                )
                            )

                            measurements.forEach { m ->
                                val cx = m.xRatio * size.width
                                val cy = m.yRatio * size.height
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.9f),
                                    radius = 16.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    radius = 16.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 1.5f)
                                )
                                val value = if (mode == HeatMapMode.TEMPERATURE) {
                                    if (isCelsius) "%.0f°C".format(m.temperature)
                                    else "%.0f°F".format(m.temperature * 9f / 5f + 32f)
                                } else "%.0f%%".format(m.humidity)

                                val textLayout = textMeasurer.measure(
                                    AnnotatedString(value),
                                    style = TextStyle(
                                        color = Color(0xFF1E293B),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayout,
                                    topLeft = Offset(
                                        cx - textLayout.size.width / 2f,
                                        cy - textLayout.size.height / 2f
                                    )
                                )
                            }
                        }
                    } ?: CircularProgressIndicator()
                }
            }

            if (mode == HeatMapMode.TEMPERATURE) {
                TemperatureLegend(isCelsius = isCelsius)
            } else {
                HumidityLegend()
            }

            Spacer(Modifier.height(8.dp))

            if (measurements.isNotEmpty()) {
                val avgTemp = measurements.map { it.temperature }.average().toFloat()
                val avgHum = measurements.map { it.humidity }.average().toFloat()
                val minTemp = measurements.minOf { it.temperature }
                val maxTemp = measurements.maxOf { it.temperature }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatPill("Min", if (isCelsius) "%.1f°C".format(minTemp) else "%.1f°F".format(minTemp * 9f / 5f + 32f), ColdZone)
                    StatPill("Avg", if (isCelsius) "%.1f°C".format(avgTemp) else "%.1f°F".format(avgTemp * 9f / 5f + 32f), NeutralZone)
                    StatPill("Max", if (isCelsius) "%.1f°C".format(maxTemp) else "%.1f°F".format(maxTemp * 9f / 5f + 32f), HotZone)
                    StatPill("Humidity", "%.0f%%".format(avgHum), AccentCyan)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TemperatureLegend(isCelsius: Boolean) {
    val steps = if (isCelsius) listOf(10f, 16f, 18f, 20f, 24f, 28f, 35f)
    else listOf(50f, 61f, 64f, 68f, 75f, 82f, 95f)
    val labels = if (isCelsius) listOf("10", "16", "18", "20", "24", "28", "35+")
    else listOf("50", "61", "64", "68", "75", "82", "95+")
    val colors = listOf(
        Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF38BDF8), Color(0xFF34D399),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFDC2626)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "Temperature Scale",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    Brush.horizontalGradient(colors = colors),
                    RoundedCornerShape(8.dp)
                )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { l ->
                Text(
                    text = l,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HumidityLegend() {
    val colors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF34D399), Color(0xFF38BDF8), Color(0xFF1D4ED8))
    val labels = listOf("0%", "30%", "40-60%", "70%", "100%")
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "Humidity Scale",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    Brush.horizontalGradient(colors = colors),
                    RoundedCornerShape(8.dp)
                )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { l ->
                Text(l, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
