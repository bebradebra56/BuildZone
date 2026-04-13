package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.db.entity.TaskEntity
import com.buildzone.zonebu.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    val tasks: Flow<List<TaskEntity>> = taskRepository.getAllTasks()
    val pendingCount: Flow<Int> = taskRepository.getPendingTaskCount()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun createTask(
        title: String, description: String,
        projectId: Long? = null, dueDate: Long? = null, priority: Int = 1
    ) {
        viewModelScope.launch {
            taskRepository.createTask(title, description, projectId, dueDate, priority)
            _saved.value = true
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { taskRepository.updateTask(task) }
    }

    fun toggleTask(id: Long, title: String, completed: Boolean) {
        viewModelScope.launch { taskRepository.toggleTask(id, title, completed) }
    }

    fun deleteTask(id: Long, title: String) {
        viewModelScope.launch { taskRepository.deleteTask(id, title) }
    }

    fun clearSaved() { _saved.value = false }
}
