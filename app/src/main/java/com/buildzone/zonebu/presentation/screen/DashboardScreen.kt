package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.buildzone.zonebu.presentation.navigation.Routes
import com.buildzone.zonebu.presentation.viewmodel.DashboardViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(navController: NavController) {
    val viewModel: DashboardViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()

    val projectCount by viewModel.projectCount.collectAsState(0)
    val roomCount by viewModel.totalRoomCount.collectAsState(0)
    val measurementCount by viewModel.totalMeasurementCount.collectAsState(0)
    val recentMeasurements by viewModel.recentMeasurements.collectAsState(emptyList())
    val pendingTasks by viewModel.pendingTaskCount.collectAsState(0)

    val isCelsius = settings.temperatureUnit.name == "CELSIUS"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your home climate overview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        modifier = Modifier.width(150.dp),
                        title = "Projects",
                        value = projectCount.toString(),
                        icon = Icons.Filled.AccountTree,
                        color = AccentBlue
                    )
                }
                item {
                    StatCard(
                        modifier = Modifier.width(150.dp),
                        title = "Rooms",
                        value = roomCount.toString(),
                        icon = Icons.Filled.Home,
                        color = AccentIndigo
                    )
                }
                item {
                    StatCard(
                        modifier = Modifier.width(150.dp),
                        title = "Measurements",
                        value = measurementCount.toString(),
                        icon = Icons.Filled.Thermostat,
                        color = AccentCyan
                    )
                }
                item {
                    StatCard(
                        modifier = Modifier.width(150.dp),
                        title = "Pending Tasks",
                        value = pendingTasks.toString(),
                        icon = Icons.Filled.CheckBox,
                        color = if (pendingTasks > 0) WarmZone else NeutralZone
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            SectionHeader(
                title = "Recent Measurements",
                action = {
                    TextButton(onClick = { navController.navigate(Routes.PROJECTS) }) {
                        Text("View All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }

        if (recentMeasurements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    EmptyState(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Thermostat,
                        title = "No measurements yet",
                        subtitle = "Add rooms and start measuring temperature and humidity"
                    )
                }
            }
        } else {
            items(recentMeasurements) { measurement ->
                MeasurementListItem(measurement, isCelsius)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            SectionHeader(title = "Quick Actions")
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "New Project",
                    icon = Icons.Filled.Add,
                    color = AccentBlue,
                    onClick = { navController.navigate(Routes.ADD_PROJECT) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "New Task",
                    icon = Icons.Filled.AddTask,
                    color = AccentIndigo,
                    onClick = { navController.navigate(Routes.ADD_TASK) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Reports",
                    icon = Icons.Filled.BarChart,
                    color = AccentCyan,
                    onClick = { navController.navigate(Routes.REPORTS) }
                )
            }
        }
    }
}

@Composable
private fun MeasurementListItem(measurement: MeasurementEntity, isCelsius: Boolean) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.ENGLISH) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ZoneColorDot(measurement.temperature, size = 14.dp)
                Column {
                    Text(
                        text = measurement.label.ifEmpty { "Point (%.0f%%, %.0f%%)".format(measurement.xRatio * 100, measurement.yRatio * 100) },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateFormat.format(Date(measurement.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TemperatureChip(measurement.temperature, isCelsius)
                HumidityChip(measurement.humidity)
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
