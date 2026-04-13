package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.data.db.entity.RoomEntity
import com.buildzone.zonebu.presentation.components.AppTopBar
import com.buildzone.zonebu.presentation.components.EmptyState
import com.buildzone.zonebu.presentation.viewmodel.MeasurementViewModel
import com.buildzone.zonebu.presentation.viewmodel.RoomViewModel
import com.buildzone.zonebu.ui.theme.temperatureToColor
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RoomPlanScreen(navController: NavController, roomId: Long) {
    val roomViewModel: RoomViewModel = koinViewModel(parameters = { parametersOf(0L) })
    val measurementViewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })

    val room by roomViewModel.getRoomById(roomId).collectAsState(null)
    val measurements by measurementViewModel.measurements.collectAsState(emptyList())

    var selectedMeasurement by remember { mutableStateOf<MeasurementEntity?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = room?.name ?: "Room Plan",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("heat_map/$roomId") }) {
                        Icon(Icons.Filled.Thermostat, contentDescription = "Heat Map")
                    }
                    IconButton(onClick = { navController.navigate("problem_zones/$roomId") }) {
                        Icon(Icons.Filled.Warning, contentDescription = "Problem Zones")
                    }
                    IconButton(onClick = { navController.navigate("measurements_list/$roomId") }) {
                        Icon(Icons.Filled.List, contentDescription = "Measurements List")
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
            room?.let { currentRoom ->
                Text(
                    text = "Tap anywhere on the floor plan to add a measurement point",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                RoomPlanCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    room = currentRoom,
                    measurements = measurements,
                    onTap = { xRatio, yRatio ->
                        val xInt = (xRatio * 1000).toInt()
                        val yInt = (yRatio * 1000).toInt()
                        navController.navigate("add_measurement/$roomId/$xInt/$yInt")
                    },
                    onMeasurementTap = { measurement ->
                        selectedMeasurement = measurement
                    }
                )

                if (measurements.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.AddLocation,
                        title = "No measurements yet",
                        subtitle = "Tap on the floor plan to add a point"
                    )
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    selectedMeasurement?.let { m ->
        MeasurementPointDialog(
            measurement = m,
            onEdit = {
                selectedMeasurement = null
                navController.navigate("edit_measurement/${m.id}")
            },
            onDelete = {
                selectedMeasurement = null
                measurementViewModel.deleteMeasurement(m.id)
            },
            onDismiss = { selectedMeasurement = null }
        )
    }
}

@Composable
private fun RoomPlanCanvas(
    modifier: Modifier = Modifier,
    room: RoomEntity,
    measurements: List<MeasurementEntity>,
    onTap: (Float, Float) -> Unit,
    onMeasurementTap: (MeasurementEntity) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
    ) {
        val aspectRatio = (room.widthMeters / room.heightMeters).coerceIn(0.3f, 3f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .align(Alignment.Center)
                .pointerInput(measurements) {
                    detectTapGestures { offset ->
                        val xRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        val yRatio = (offset.y / size.height).coerceIn(0f, 1f)
                        val tapped = measurements.find { m ->
                            val mx = m.xRatio * size.width
                            val my = m.yRatio * size.height
                            val d = kotlin.math.sqrt((offset.x - mx).pow2() + (offset.y - my).pow2())
                            d < 40f
                        }
                        if (tapped != null) onMeasurementTap(tapped)
                        else onTap(xRatio, yRatio)
                    }
                }
        ) {
            val borderColor = Color(0xFF3B82F6)
            val gridColor = Color(0xFF94A3B8).copy(alpha = 0.3f)
            val cellW = size.width / 5f
            val cellH = size.height / 5f

            for (i in 1..4) {
                drawLine(gridColor, Offset(cellW * i, 0f), Offset(cellW * i, size.height), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, cellH * i), Offset(size.width, cellH * i), strokeWidth = 1f)
            }

            drawRect(
                color = borderColor,
                topLeft = Offset(0f, 0f),
                size = size,
                style = Stroke(width = 3f)
            )

            measurements.forEach { m ->
                val cx = m.xRatio * size.width
                val cy = m.yRatio * size.height
                val color = temperatureToColor(m.temperature)

                drawCircle(color.copy(alpha = 0.25f), radius = 28.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color, radius = 14.dp.toPx(), center = Offset(cx, cy))
                drawCircle(Color.White, radius = 14.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 2f))

                val label = "%.0f°".format(m.temperature)
                val textLayout = textMeasurer.measure(
                    AnnotatedString(label),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 10.sp,
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

                if (m.label.isNotEmpty()) {
                    val labelLayout = textMeasurer.measure(
                        AnnotatedString(m.label),
                        style = TextStyle(color = Color(0xFF1E293B), fontSize = 9.sp)
                    )
                    drawText(
                        labelLayout,
                        topLeft = Offset(
                            (cx - labelLayout.size.width / 2f).coerceIn(0f, size.width - labelLayout.size.width),
                            cy + 18.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

private fun Float.pow2() = this * this

@Composable
private fun MeasurementPointDialog(
    measurement: MeasurementEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                measurement.label.ifEmpty { "Measurement Point" },
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = temperatureToColor(measurement.temperature).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "%.1f°C".format(measurement.temperature),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = temperatureToColor(measurement.temperature)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "%.0f%% RH".format(measurement.humidity),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                if (measurement.notes.isNotEmpty()) {
                    Text(
                        measurement.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) { Text("Edit") }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
