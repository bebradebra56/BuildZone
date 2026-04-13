package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.presentation.components.AppTopBar
import com.buildzone.zonebu.presentation.viewmodel.MeasurementViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.temperatureToColor
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddMeasurementScreen(
    navController: NavController,
    roomId: Long,
    xRatio: Float,
    yRatio: Float
) {
    val viewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val saved by viewModel.saved.collectAsState()

    var tempInput by remember { mutableStateOf("20") }
    var humidityInput by remember { mutableStateOf("50") }
    var label by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var tempError by remember { mutableStateOf(false) }
    var humidityError by remember { mutableStateOf(false) }

    val tempCelsius = tempInput.toFloatOrNull()?.let { v ->
        if (isCelsius) v else (v - 32f) * 5f / 9f
    } ?: 20f

    LaunchedEffect(saved) {
        if (saved) {
            viewModel.clearSaved()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add Measurement",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = temperatureToColor(tempCelsius).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(temperatureToColor(tempCelsius).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AddLocation,
                            contentDescription = null,
                            tint = temperatureToColor(tempCelsius),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Position",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "X: %.0f%%  Y: %.0f%%".format(xRatio * 100, yRatio * 100),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = tempInput,
                onValueChange = { tempInput = it; tempError = false },
                label = { Text("Temperature (${if (isCelsius) "°C" else "°F"}) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = tempError,
                supportingText = {
                    if (tempError) Text("Valid temperature required")
                    else {
                        val t = tempInput.toFloatOrNull()
                        if (t != null) {
                            val color = temperatureToColor(if (isCelsius) t else (t - 32f) * 5f / 9f)
                            val status = when {
                                tempCelsius < 16f -> "Cold"
                                tempCelsius < 20f -> "Cool"
                                tempCelsius < 24f -> "Normal"
                                tempCelsius < 28f -> "Warm"
                                else -> "Hot"
                            }
                            Text(status, color = color)
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Thermostat,
                        null,
                        tint = if (tempInput.toFloatOrNull() != null) temperatureToColor(tempCelsius)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = humidityInput,
                onValueChange = { humidityInput = it; humidityError = false },
                label = { Text("Humidity (%) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = humidityError,
                supportingText = {
                    if (humidityError) Text("Valid humidity (0-100) required")
                    else {
                        val h = humidityInput.toFloatOrNull()
                        if (h != null) {
                            val status = when {
                                h < 30f -> "Too dry"
                                h < 40f -> "Dry"
                                h < 60f -> "Comfortable"
                                h < 70f -> "Humid"
                                else -> "Too humid — mold risk!"
                            }
                            Text(status, color = if (h > 65f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                leadingIcon = { Icon(Icons.Filled.WaterDrop, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (optional)") },
                placeholder = { Text("e.g. Near window, Corner wall") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Label, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val t = tempInput.toFloatOrNull()
                    val h = humidityInput.toFloatOrNull()
                    if (t == null) { tempError = true; return@Button }
                    if (h == null || h < 0 || h > 100) { humidityError = true; return@Button }
                    val tCelsius = if (isCelsius) t else (t - 32f) * 5f / 9f
                    viewModel.addMeasurement(xRatio, yRatio, tCelsius, h, label.trim(), notes.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Measurement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EditMeasurementScreen(navController: NavController, measurementId: Long) {
    val measurementViewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(0L) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val measurement by measurementViewModel.getMeasurementById(measurementId).collectAsState(null)

    var tempInput by remember { mutableStateOf("") }
    var humidityInput by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(measurement) {
        if (measurement != null && !initialized) {
            initialized = true
            val m = measurement!!
            tempInput = if (isCelsius) "%.1f".format(m.temperature)
            else "%.1f".format(m.temperature * 9f / 5f + 32f)
            humidityInput = "%.1f".format(m.humidity)
            label = m.label
            notes = m.notes
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Edit Measurement", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = tempInput,
                onValueChange = { tempInput = it },
                label = { Text("Temperature (${if (isCelsius) "°C" else "°F"})") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = humidityInput,
                onValueChange = { humidityInput = it },
                label = { Text("Humidity (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val m = measurement ?: return@Button
                    val t = tempInput.toFloatOrNull() ?: return@Button
                    val h = humidityInput.toFloatOrNull() ?: return@Button
                    val tCelsius = if (isCelsius) t else (t - 32f) * 5f / 9f
                    measurementViewModel.updateMeasurement(
                        m.copy(temperature = tCelsius, humidity = h, label = label.trim(), notes = notes.trim())
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
