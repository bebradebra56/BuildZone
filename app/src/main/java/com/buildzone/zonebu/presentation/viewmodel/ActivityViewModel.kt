package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.db.entity.ActivityLogEntity
import com.buildzone.zonebu.data.repository.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ActivityViewModel(private val activityLogRepository: ActivityLogRepository) : ViewModel() {

    val logs: Flow<List<ActivityLogEntity>> = activityLogRepository.getAllLogs()

    val recentLogs: Flow<List<ActivityLogEntity>> = activityLogRepository.getRecentLogs(30)

    fun clearAll() {
        viewModelScope.launch { activityLogRepository.clearAll() }
    }
}
