package com.buildzone.zonebu.presentation.screen

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.ProjectEntity
import com.buildzone.zonebu.presentation.components.*
import com.buildzone.zonebu.presentation.viewmodel.ProjectReport
import com.buildzone.zonebu.presentation.viewmodel.ReportsViewModel
import com.buildzone.zonebu.presentation.viewmodel.RoomStats
import com.buildzone.zonebu.presentation.viewmodel.MeasurementViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.*
import kotlinx.coroutines.Dispatchers
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(navController: NavController) {
    val viewModel: ReportsViewModel = koinViewModel()
    val projects by viewModel.projects.collectAsState(emptyList())
    val selectedId by viewModel.selectedProjectId.collectAsState()

    val selectedReport by if (selectedId != null)
        viewModel.getReportForProject(selectedId!!).collectAsState(null)
    else remember { mutableStateOf(null) }

    Scaffold(
        topBar = { AppTopBar(title = "Reports") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.BarChart,
                        title = "No projects yet",
                        subtitle = "Create a project and add measurements to see reports"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            "Select a project",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        ProjectSelector(
                            projects = projects,
                            selectedId = selectedId,
                            onSelect = { viewModel.selectProject(it) }
                        )
                    }

                    selectedReport?.let { report ->
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            ReportOverviewCard(report, navController)
                        }
                        item { SectionHeader("Room Details") }
                        items(report.roomStats) { stats ->
                            RoomStatsCard(stats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSelector(
    projects: List<ProjectEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        projects.forEach { project ->
            FilterChip(
                selected = selectedId == project.id,
                onClick = { onSelect(project.id) },
                label = { Text(project.name, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@Composable
private fun ReportOverviewCard(report: ProjectReport, navController: NavController) {
    val firstRoomId = report.roomStats.firstOrNull()?.room?.id
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.BarChart, null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                Text(
                    report.project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ReportStat("Rooms", report.roomStats.size.toString())
                ReportStat("Measurements", report.totalMeasurements.toString())
                ReportStat("Avg Temp", "%.1f°C".format(report.overallAvgTemp))
                ReportStat("Avg Humidity", "%.0f%%".format(report.overallAvgHumidity))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { navController.navigate("export/${report.project.id}") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export")
                }
                OutlinedButton(
                    onClick = { firstRoomId?.let { navController.navigate("comparison/$it") } },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    enabled = firstRoomId != null
                ) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Before/After")
                }
            }
        }
    }
}

@Composable
private fun ReportStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RoomStatsCard(stats: RoomStats) {
    val problemColor = when {
        stats.problemCount == 0 -> NeutralZone
        stats.problemCount < 3 -> WarmZone
        else -> ErrorRed
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stats.room.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (stats.problemCount > 0) {
                    ProblemBadge("${stats.problemCount} issues", problemColor)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoomStatChip("%.1f°C".format(stats.avgTemp), "Avg temp", temperatureToColor(stats.avgTemp))
                RoomStatChip("%.0f%%".format(stats.avgHumidity), "Avg humidity", AccentCyan)
                RoomStatChip("${stats.measurements.size}", "Points", AccentIndigo)
            }
            if (stats.measurements.isNotEmpty()) {
                Text(
                    "Range: %.1f – %.1f°C".format(stats.minTemp, stats.maxTemp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RoomStatChip(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ExportScreen(navController: NavController, projectId: Long) {
    val viewModel: ReportsViewModel = koinViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()

    val report by viewModel.getReportForProject(projectId).collectAsState(null)

    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "Export Report", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            report?.let { r ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Report Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(buildReportText(r, settings.temperatureUnit.name == "CELSIUS"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.heightIn(max = 300.dp)
                        )
                    }
                }
            } ?: CircularProgressIndicator()

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val r = report ?: return@Button
                    isExporting = true
                    scope.launch {
                        try {
                            val text = buildReportText(r, settings.temperatureUnit.name == "CELSIUS")
                            val fileName = "buildzone_report_${System.currentTimeMillis()}.txt"
                            val file = withContext(Dispatchers.IO) {
                                val dir = File(context.filesDir, "reports").also { it.mkdirs() }
                                File(dir, fileName).also { it.writeText(text) }
                            }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "BuildZone Report: ${r.project.name}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        } finally {
                            isExporting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isExporting && report != null
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun buildReportText(report: ProjectReport, isCelsius: Boolean): String {
    val dateFmt = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.ENGLISH)
    val unit = if (isCelsius) "°C" else "°F"
    val sb = StringBuilder()
    sb.appendLine("═══════════════════════════════")
    sb.appendLine("  BUILDZONE CLIMATE REPORT")
    sb.appendLine("═══════════════════════════════")
    sb.appendLine("Project: ${report.project.name}")
    sb.appendLine("Generated: ${dateFmt.format(Date())}")
    sb.appendLine()
    sb.appendLine("SUMMARY")
    sb.appendLine("───────────────────────────────")
    sb.appendLine("Rooms: ${report.roomStats.size}")
    sb.appendLine("Total measurements: ${report.totalMeasurements}")
    sb.appendLine("Overall avg temperature: %.1f$unit".format(
        if (isCelsius) report.overallAvgTemp else report.overallAvgTemp * 9f / 5f + 32f
    ))
    sb.appendLine("Overall avg humidity: %.0f%%".format(report.overallAvgHumidity))
    sb.appendLine()
    report.roomStats.forEach { stats ->
        sb.appendLine("ROOM: ${stats.room.name}")
        sb.appendLine("  Dimensions: %.1fm × %.1fm".format(stats.room.widthMeters, stats.room.heightMeters))
        sb.appendLine("  Measurements: ${stats.measurements.size}")
        sb.appendLine("  Avg temperature: %.1f$unit".format(
            if (isCelsius) stats.avgTemp else stats.avgTemp * 9f / 5f + 32f
        ))
        sb.appendLine("  Min/Max: %.1f / %.1f$unit".format(
            if (isCelsius) stats.minTemp else stats.minTemp * 9f / 5f + 32f,
            if (isCelsius) stats.maxTemp else stats.maxTemp * 9f / 5f + 32f
        ))
        sb.appendLine("  Avg humidity: %.0f%%".format(stats.avgHumidity))
        if (stats.problemCount > 0) sb.appendLine("  ⚠ ${stats.problemCount} problem zone(s) detected")
        sb.appendLine()
    }
    sb.appendLine("═══════════════════════════════")
    return sb.toString()
}

@Composable
fun ComparisonScreen(navController: NavController, roomId: Long) {
    val viewModel: MeasurementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    val allMeasurements by viewModel.measurements.collectAsState(emptyList())

    val now = remember { System.currentTimeMillis() }
    val dayMs = 24L * 60L * 60L * 1000L

    val recentMeasurements = allMeasurements.filter { it.timestamp > now - 7 * dayMs }
    val olderMeasurements = allMeasurements.filter { it.timestamp <= now - 7 * dayMs }

    Scaffold(
        topBar = { AppTopBar(title = "Before / After", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Comparing last 7 days vs. older measurements",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ComparisonCard(
                    modifier = Modifier.weight(1f),
                    title = "Before",
                    subtitle = "Older measurements",
                    temps = olderMeasurements.map { it.temperature },
                    humidities = olderMeasurements.map { it.humidity },
                    count = olderMeasurements.size,
                    isCelsius = isCelsius,
                    color = AccentIndigo
                )
                ComparisonCard(
                    modifier = Modifier.weight(1f),
                    title = "After",
                    subtitle = "Last 7 days",
                    temps = recentMeasurements.map { it.temperature },
                    humidities = recentMeasurements.map { it.humidity },
                    count = recentMeasurements.size,
                    isCelsius = isCelsius,
                    color = AccentBlue
                )
            }

            if (recentMeasurements.isNotEmpty() && olderMeasurements.isNotEmpty()) {
                val oldAvgTemp = olderMeasurements.map { it.temperature }.average().toFloat()
                val newAvgTemp = recentMeasurements.map { it.temperature }.average().toFloat()
                val diff = newAvgTemp - oldAvgTemp

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (diff < 0) ColdZone.copy(alpha = 0.1f) else HotZone.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (diff < 0) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                            null,
                            tint = if (diff < 0) ColdZone else HotZone,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Temperature change",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val displayDiff = if (isCelsius) diff else diff * 9f / 5f
                            Text(
                                "${if (diff >= 0) "+" else ""}%.1f${if (isCelsius) "°C" else "°F"}".format(displayDiff),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (diff < 0) ColdZone else HotZone
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    temps: List<Float>,
    humidities: List<Float>,
    count: Int,
    isCelsius: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = color.copy(alpha = 0.3f))
            if (count == 0) {
                Text("No data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val avgTemp = temps.average().toFloat()
                val avgHum = humidities.average().toFloat()
                Text(
                    if (isCelsius) "%.1f°C".format(avgTemp) else "%.1f°F".format(avgTemp * 9f / 5f + 32f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = temperatureToColor(avgTemp)
                )
                Text("%.0f%% RH".format(avgHum), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$count points", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
