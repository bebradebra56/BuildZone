package com.buildzone.zonebu.data.repository

import com.buildzone.zonebu.data.db.dao.ActivityLogDao
import com.buildzone.zonebu.data.db.dao.TaskDao
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import com.buildzone.zonebu.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val activityLogDao: ActivityLogDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTaskById(id: Long): Flow<TaskEntity?> = taskDao.getTaskById(id)

    fun getPendingTaskCount(): Flow<Int> = taskDao.getPendingTaskCount()

    suspend fun createTask(
        title: String, description: String,
        projectId: Long?, dueDate: Long?, priority: Int
    ): Long {
        val id = taskDao.insertTask(
            TaskEntity(
                title = title, description = description,
                projectId = projectId, dueDate = dueDate, priority = priority
            )
        )
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "TASK", entityId = id,
                action = "CREATED", description = "Task created: $title"
            )
        )
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun toggleTask(id: Long, title: String, completed: Boolean) {
        taskDao.setTaskCompleted(id, completed)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "TASK", entityId = id,
                action = if (completed) "COMPLETED" else "REOPENED",
                description = if (completed) "Task completed: $title" else "Task reopened: $title"
            )
        )
    }

    suspend fun deleteTask(id: Long, title: String) {
        taskDao.deleteTaskById(id)
        activityLogDao.insertLog(
            ActivityLogEntity(
                entityType = "TASK", entityId = id,
                action = "DELETED", description = "Task deleted: $title"
            )
        )
    }
}
