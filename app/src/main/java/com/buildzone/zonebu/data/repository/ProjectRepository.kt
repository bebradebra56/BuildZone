package com.buildzone.zonebu.data.repository

import com.buildzone.zonebu.data.db.dao.ActivityLogDao
import com.buildzone.zonebu.data.db.dao.ProjectDao
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import com.buildzone.zonebu.data.db.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val activityLogDao: ActivityLogDao
) {
    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun getProjectById(id: Long): Flow<ProjectEntity?> = projectDao.getProjectById(id)

    fun getProjectCount(): Flow<Int> = projectDao.getProjectCount()

    suspend fun createProject(name: String, description: String): Long {
        val id = projectDao.insertProject(ProjectEntity(name = name, description = description))
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "PROJECT", entityId = id,
                action = "CREATED", description = "Project created: $name"
            )
        )
        return id
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "PROJECT", entityId = project.id,
                action = "UPDATED", description = "Project updated: ${project.name}"
            )
        )
    }

    suspend fun deleteProject(id: Long, name: String) {
        projectDao.deleteProjectById(id)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "PROJECT", entityId = id,
                action = "DELETED", description = "Project deleted: $name"
            )
        )
    }
}
