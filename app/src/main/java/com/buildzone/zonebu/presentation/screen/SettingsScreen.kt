package com.buildzone.zonebu.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.datastore.LengthUnit
import com.buildzone.zonebu.data.datastore.TemperatureUnit
import com.buildzone.zonebu.data.datastore.ThemeMode
import com.buildzone.zonebu.presentation.components.AppTopBar
import com.buildzone.zonebu.presentation.navigation.Routes
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = koinViewModel()
    val settings by viewModel.settings.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = { AppTopBar(title = "Settings") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionHeader("Units")
            }

            item {
                SettingsSegmentRow(
                    icon = Icons.Filled.Thermostat,
                    title = "Temperature",
                    options = listOf("Celsius" to TemperatureUnit.CELSIUS, "Fahrenheit" to TemperatureUnit.FAHRENHEIT),
                    selected = settings.temperatureUnit,
                    onSelect = { viewModel.setTemperatureUnit(it) }
                )
            }

            item {
                SettingsSegmentRow(
                    icon = Icons.Filled.Straighten,
                    title = "Room Dimensions",
                    options = listOf("Meters" to LengthUnit.METRIC, "Feet" to LengthUnit.IMPERIAL),
                    selected = settings.lengthUnit,
                    onSelect = { viewModel.setLengthUnit(it) }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SettingsSectionHeader("Appearance") }

            item {
                SettingsSegmentRow(
                    icon = Icons.Filled.Palette,
                    title = "Theme",
                    options = listOf("Light" to ThemeMode.LIGHT, "Dark" to ThemeMode.DARK, "System" to ThemeMode.SYSTEM),
                    selected = settings.themeMode,
                    onSelect = { viewModel.setThemeMode(it) }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SettingsSectionHeader("Account") }

            item {
                SettingsNavItem(
                    icon = Icons.Filled.Person,
                    title = "Profile",
                    subtitle = settings.userName.ifEmpty { "Set your name" },
                    onClick = { navController.navigate(Routes.PROFILE) }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SettingsSectionHeader("Data") }

            item {
                SettingsNavItem(
                    icon = Icons.Filled.History,
                    title = "Activity History",
                    subtitle = "View all recorded actions",
                    onClick = { navController.navigate(Routes.ACTIVITY_HISTORY) }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SettingsSectionHeader("About") }
            item {
                SettingsNavItem(
                    icon = Icons.Filled.Policy,
                    title = "Privacy Policy",
                    subtitle = "Tap to read",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://builldzone.com/privacy-policy.html"))
                        context.startActivity(intent)
                    }
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("BuildZone", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("Version 1.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Home Climate Mapper", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun <T> SettingsSegmentRow(
    icon: ImageVector,
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (label, value) ->
                    SegmentedButton(
                        selected = selected == value,
                        onClick = { onSelect(value) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
