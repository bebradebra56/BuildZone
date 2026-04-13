package com.buildzone.zonebu.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.buildzone.zonebu.data.db.entity.ProjectEntity
import com.buildzone.zonebu.data.db.entity.RoomEntity
import com.buildzone.zonebu.presentation.components.*
import com.buildzone.zonebu.presentation.navigation.Routes
import com.buildzone.zonebu.presentation.viewmodel.ProjectViewModel
import com.buildzone.zonebu.presentation.viewmodel.RoomViewModel
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import com.buildzone.zonebu.ui.theme.AccentIndigo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RoomsScreen(navController: NavController, projectId: Long) {
    val viewModel: RoomViewModel = koinViewModel(parameters = { parametersOf(projectId) })
    val projectViewModel: ProjectViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()

    val rooms by viewModel.rooms.collectAsState(emptyList())
    val project by projectViewModel.getProjectById(projectId).collectAsState(null)
    val isMetric = settings.lengthUnit.name == "METRIC"
    val unit = if (isMetric) "m" else "ft"

    Scaffold(
        topBar = {
            AppTopBar(
                title = project?.name ?: "Rooms",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate("reports") }) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Reports")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add_room/$projectId") },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Room") },
                containerColor = AccentIndigo,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        }
    ) { padding ->
        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.Home,
                    title = "No rooms added yet",
                    subtitle = "Add a room to start measuring and mapping climate zones",
                    action = {
                        Button(onClick = { navController.navigate("add_room/$projectId") }) {
                            Text("Add Room")
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms, key = { it.id }) { room ->
                    RoomCard(
                        room = room,
                        unit = unit,
                        isMetric = isMetric,
                        onClick = { navController.navigate("room_plan/${room.id}") },
                        onHeatMap = { navController.navigate("heat_map/${room.id}") },
                        onDelete = { viewModel.deleteRoom(room.id, room.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomCard(
    room: RoomEntity,
    unit: String,
    isMetric: Boolean,
    onClick: () -> Unit,
    onHeatMap: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val displayW = if (isMetric) room.widthMeters else room.widthMeters * 3.281f
    val displayH = if (isMetric) room.heightMeters else room.heightMeters * 3.281f

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentIndigo.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,
                            tint = AccentIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "%.1f × %.1f $unit".format(displayW, displayH),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Heat Map") },
                            onClick = { showMenu = false; onHeatMap() },
                            leadingIcon = { Icon(Icons.Filled.Thermostat, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
            if (room.description.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = room.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onClick,
                    label = { Text("Floor Plan", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.Map, null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = onHeatMap,
                    label = { Text("Heat Map", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.Thermostat, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun AddRoomScreen(navController: NavController, projectId: Long) {
    val viewModel: RoomViewModel = koinViewModel(parameters = { parametersOf(projectId) })
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val isMetric = settings.lengthUnit.name == "METRIC"
    val unit = if (isMetric) "m" else "ft"

    val newRoomId by viewModel.newRoomId.collectAsState()

    var name by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("4") }
    var height by remember { mutableStateOf("3") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(newRoomId) {
        if (newRoomId != null) {
            viewModel.clearNewRoomId()
            navController.navigate("room_plan/$newRoomId") {
                popUpTo("add_room/$projectId") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Add Room", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Room Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = { if (nameError) Text("Name is required") },
                leadingIcon = { Icon(Icons.Filled.Home, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Width ($unit)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height ($unit)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    val w = width.toFloatOrNull()?.let { if (isMetric) it else it / 3.281f } ?: 4f
                    val h = height.toFloatOrNull()?.let { if (isMetric) it else it / 3.281f } ?: 3f
                    viewModel.createRoom(name.trim(), w, h, description.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create Room", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
