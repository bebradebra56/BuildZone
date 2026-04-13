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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.MeasurementEntity
import com.buildzone.zonebu.presentation.components.*
import com.buildzone.zonebu.presentation.viewmodel.MeasurementViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

enum class ZoneType(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
) {
    COLD("Cold Spot", "Temperature below 18°C", Icons.Filled.AcUnit, ColdZone),
    HOT("Hot Spot", "Temperature above 26°C", Icons.Filled.LocalFireDepartment, HotZone),
    MOLD_RISK("Mold Risk", "High humidity + low temp", Icons.Filled.BugReport, Color(0xFF7C3AED)),
    DRAFT("Draft Area", "Large temp difference nearby", Icons.Filled.Air, CoolZone),
    DRY("Dry Zone", "Humidity below 30%", Icons.Filled.WbSunny, WarmZone)
}

data class ProblemZone(
    val measurement: MeasurementEntity,
    val zoneType: ZoneType,
    val severity: Float
)

fun detectProblemZones(measurements: List<MeasurementEntity>): List<ProblemZone> {
    val zones = mutableListOf<ProblemZone>()
    measurements.forEach { m ->
        when {
            m.temperature < 16f -> zones.add(ProblemZone(m, ZoneType.COLD, (16f - m.temperature) / 16f))
            m.temperature < 18f -> zones.add(ProblemZone(m, ZoneType.COLD, (18f - m.temperature) / 8f * 0.5f))
            m.temperature > 28f -> zones.add(ProblemZone(m, ZoneType.HOT, (m.temperature - 28f) / 10f))
            m.temperature > 26f -> zones.add(ProblemZone(m, ZoneType.HOT, (m.temperature - 26f) / 4f * 0.5f))
        }
        if (m.humidity > 65f && m.temperature < 22f) {
            zones.add(ProblemZone(m, ZoneType.MOLD_RISK, (m.humidity - 65f) / 35f))
        }
        if (m.humidity < 30f) {
            zones.add(ProblemZone(m, ZoneType.DRY, (30f - m.humidity) / 30f))
        }
    }
    if (measurements.size >= 2) {
        val avgTemp = measurements.map { it.temperature }.average().toFloat()
        measurements.forEach { m ->
            if (kotlin.math.abs(m.temperature - avgTemp) > 5f) {
                zones.add(ProblemZone(m, ZoneType.DRAFT, kotlin.math.abs(m.temperature - avgTemp) / 10f))
            }
        }
    }
    return zones.sortedByDescending { it.severity }
}

@Composable
fun ProblemZonesScreen(navController: NavController, roomId: Long) {
    val viewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val measurements by viewModel.measurements.collectAsState(emptyList())
    val problemZones = remember(measurements) { detectProblemZones(measurements) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Problem Zones",
                onBack = { navController.popBackStack() }
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
                    icon = Icons.Filled.CheckCircle,
                    title = "No data available",
                    subtitle = "Add measurements to detect problem zones"
                )
            }
        } else if (problemZones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.CheckCircle,
                    title = "No problems detected!",
                    subtitle = "All measurements look healthy. Temperature and humidity are in ideal range."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "${problemZones.size} problem(s) detected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                items(problemZones) { zone ->
                    ProblemZoneCard(
                        zone = zone,
                        isCelsius = isCelsius,
                        onClick = {
                            navController.navigate("zone_details/$roomId/${zone.zoneType.name}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemZoneCard(
    zone: ProblemZone,
    isCelsius: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = zone.zoneType.color.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, zone.zoneType.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = zone.zoneType.color.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        zone.zoneType.icon,
                        contentDescription = null,
                        tint = zone.zoneType.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        zone.zoneType.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = zone.zoneType.color
                    )
                    if (zone.severity > 0.6f) {
                        ProblemBadge("Critical", ErrorRed)
                    }
                }
                Text(
                    zone.zoneType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val label = zone.measurement.label.ifEmpty { "Pos: %.0f%%, %.0f%%".format(zone.measurement.xRatio * 100, zone.measurement.yRatio * 100) }
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TemperatureChip(zone.measurement.temperature, isCelsius)
                HumidityChip(zone.measurement.humidity)
            }
        }
    }
}

@Composable
fun ZoneDetailsScreen(navController: NavController, roomId: Long, zoneType: String) {
    val viewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val measurements by viewModel.measurements.collectAsState(emptyList())
    val zone = ZoneType.entries.find { it.name == zoneType } ?: ZoneType.COLD
    val affectedMeasurements = remember(measurements, zoneType) {
        detectProblemZones(measurements).filter { it.zoneType.name == zoneType }
    }

    val recommendations = getRecommendations(zone)

    Scaffold(
        topBar = {
            AppTopBar(
                title = zone.label,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = zone.color.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(zone.icon, null, tint = zone.color, modifier = Modifier.size(40.dp))
                        Column {
                            Text(zone.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = zone.color)
                            Text(zone.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${affectedMeasurements.size} point(s) affected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item { SectionHeader("Affected Measurement Points") }

            items(affectedMeasurements) { pz ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                pz.measurement.label.ifEmpty { "Pos: %.0f%%, %.0f%%".format(pz.measurement.xRatio * 100, pz.measurement.yRatio * 100) },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text("Severity: %.0f%%".format(pz.severity * 100), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TemperatureChip(pz.measurement.temperature, isCelsius)
                            HumidityChip(pz.measurement.humidity)
                        }
                    }
                }
            }

            item { SectionHeader("Recommendations") }

            items(recommendations) { rec ->
                RecommendationCard(rec)
            }
        }
    }
}

data class Recommendation(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val priority: String
)

fun getRecommendations(zone: ZoneType): List<Recommendation> = when (zone) {
    ZoneType.COLD -> listOf(
        Recommendation("Insulate the wall", "Add thermal insulation to reduce heat loss", Icons.Filled.Construction, "High"),
        Recommendation("Check window seals", "Inspect and replace weatherstripping around windows", Icons.Filled.Window, "High"),
        Recommendation("Install thermal curtains", "Heavy curtains help reduce cold drafts", Icons.Filled.DarkMode, "Medium"),
        Recommendation("Seal air gaps", "Caulk and seal any gaps around pipes and outlets", Icons.Filled.Build, "Medium")
    )
    ZoneType.HOT -> listOf(
        Recommendation("Improve ventilation", "Add or check exhaust fans and air circulation", Icons.Filled.Air, "High"),
        Recommendation("Add window shading", "Install blinds or solar film on south-facing windows", Icons.Filled.WbSunny, "High"),
        Recommendation("Check insulation", "Roof and attic insulation can prevent heat gain", Icons.Filled.Layers, "Medium"),
        Recommendation("Consider AC unit", "Install a cooling unit in frequently hot areas", Icons.Filled.Thermostat, "Medium")
    )
    ZoneType.MOLD_RISK -> listOf(
        Recommendation("Improve ventilation urgently", "Open windows daily and install exhaust fans", Icons.Filled.Air, "Critical"),
        Recommendation("Use a dehumidifier", "Keep relative humidity below 50%", Icons.Filled.WaterDrop, "High"),
        Recommendation("Inspect for leaks", "Check pipes and walls for hidden moisture", Icons.Filled.Search, "High"),
        Recommendation("Anti-mold coating", "Apply mold-resistant paint or treatment", Icons.Filled.FormatPaint, "Medium")
    )
    ZoneType.DRAFT -> listOf(
        Recommendation("Locate air leaks", "Use incense or a thermal camera to find drafts", Icons.Filled.Search, "High"),
        Recommendation("Seal windows and doors", "Use weatherstripping and door sweeps", Icons.Filled.Build, "High"),
        Recommendation("Check wall insulation", "Thermal bridges cause uneven temperatures", Icons.Filled.Layers, "Medium")
    )
    ZoneType.DRY -> listOf(
        Recommendation("Use a humidifier", "Add moisture to the air to reach 40-60% RH", Icons.Filled.WaterDrop, "High"),
        Recommendation("Add houseplants", "Plants naturally raise humidity levels", Icons.Filled.Eco, "Low"),
        Recommendation("Check heating system", "Forced air heating can significantly dry the air", Icons.Filled.Thermostat, "Medium")
    )
}

@Composable
fun RecommendationCard(rec: Recommendation) {
    val priorityColor = when (rec.priority) {
        "Critical" -> ErrorRed
        "High" -> WarmZone
        "Medium" -> AccentBlue
        else -> NeutralZone
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = priorityColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(rec.icon, null, tint = priorityColor, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(rec.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ProblemBadge(rec.priority, priorityColor)
                }
                Text(rec.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
