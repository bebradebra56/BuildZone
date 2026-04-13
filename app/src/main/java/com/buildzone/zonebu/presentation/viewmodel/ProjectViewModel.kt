package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.db.entity.ProjectEntity
import com.buildzone.zonebu.data.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val projects: Flow<List<ProjectEntity>> = projectRepository.getAllProjects()

    private val _operationResult = MutableStateFlow<Long?>(null)
    val operationResult: StateFlow<Long?> = _operationResult

    fun createProject(name: String, description: String) {
        viewModelScope.launch {
            val id = projectRepository.createProject(name, description)
            _operationResult.value = id
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch { projectRepository.updateProject(project) }
    }

    fun deleteProject(id: Long, name: String) {
        viewModelScope.launch { projectRepository.deleteProject(id, name) }
    }

    fun getProjectById(id: Long): Flow<ProjectEntity?> = projectRepository.getProjectById(id)

    fun clearResult() { _operationResult.value = null }
}
