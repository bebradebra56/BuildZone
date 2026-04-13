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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.data.db.entity.TaskEntity
import com.buildzone.zonebu.presentation.components.*
import com.buildzone.zonebu.presentation.navigation.Routes
import com.buildzone.zonebu.presentation.viewmodel.TaskViewModel
import com.buildzone.zonebu.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(navController: NavController) {
    val viewModel: TaskViewModel = koinViewModel()
    val tasks by viewModel.tasks.collectAsState(emptyList())
    val pending by viewModel.pendingCount.collectAsState(0)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Tasks",
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.ACTIVITY_HISTORY) }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_TASK) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("New Task") },
                containerColor = AccentIndigo,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.CheckBox,
                    title = "No tasks yet",
                    subtitle = "Add tasks to track home improvement actions",
                    action = {
                        Button(onClick = { navController.navigate(Routes.ADD_TASK) }) {
                            Text("Add Task")
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pending > 0) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "$pending pending task${if (pending > 1) "s" else ""}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AccentBlue
                            )
                        }
                    }
                }

                val pending_tasks = tasks.filter { !it.isCompleted }
                val completed_tasks = tasks.filter { it.isCompleted }

                if (pending_tasks.isNotEmpty()) {
                    item { SectionHeader("Pending") }
                    items(pending_tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id, task.title, !task.isCompleted) },
                            onEdit = { navController.navigate("edit_task/${task.id}") },
                            onDelete = { viewModel.deleteTask(task.id, task.title) }
                        )
                    }
                }

                if (completed_tasks.isNotEmpty()) {
                    item { SectionHeader("Completed") }
                    items(completed_tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id, task.title, !task.isCompleted) },
                            onEdit = { navController.navigate("edit_task/${task.id}") },
                            onDelete = { viewModel.deleteTask(task.id, task.title) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (task.isCompleted) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeutralZone,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    PriorityIndicator(task.priority)
                    task.dueDate?.let { due ->
                        val isOverdue = due < System.currentTimeMillis() && !task.isCompleted
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOverdue) ErrorRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Schedule, null,
                                    modifier = Modifier.size(10.dp),
                                    tint = if (isOverdue) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    dateFormat.format(Date(due)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
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
    }
}

@Composable
fun AddTaskScreen(navController: NavController) {
    TaskFormScreen(navController = navController, taskId = null)
}

@Composable
fun EditTaskScreen(navController: NavController, taskId: Long) {
    TaskFormScreen(navController = navController, taskId = taskId)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormScreen(navController: NavController, taskId: Long?) {
    val viewModel: TaskViewModel = koinViewModel()
    val saved by viewModel.saved.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var titleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(taskId == null) }

    val allTasks by viewModel.tasks.collectAsState(emptyList())
    val existingTask: TaskEntity? = if (taskId != null) allTasks.find { it.id == taskId } else null

    LaunchedEffect(existingTask?.id) {
        if (existingTask != null && !initialized) {
            initialized = true
            existingTask.let { t ->
                title = t.title
                description = t.description
                priority = t.priority
                dueDate = t.dueDate
            }
        }
    }

    LaunchedEffect(saved) {
        if (saved) {
            viewModel.clearSaved()
            navController.popBackStack()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (taskId == null) "New Task" else "Edit Task",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Task *") },
                placeholder = { Text("e.g. Fix insulation in bedroom") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                supportingText = { if (titleError) Text("Title is required") },
                leadingIcon = { Icon(Icons.Filled.CheckBox, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Text("Priority", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Low", 1 to "Medium", 2 to "High").forEach { (p, label) ->
                    val color = when (p) { 2 -> ErrorRed; 1 -> WarmZone; else -> NeutralZone }
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    dueDate?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Date(it))
                    } ?: "Set Due Date (optional)"
                )
                if (dueDate != null) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { dueDate = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    if (taskId != null) {
                        existingTask?.let {
                            viewModel.updateTask(it.copy(title = title.trim(), description = description.trim(), priority = priority, dueDate = dueDate))
                        }
                        navController.popBackStack()
                    } else {
                        viewModel.createTask(title.trim(), description.trim(), null, dueDate, priority)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (taskId == null) "Create Task" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

