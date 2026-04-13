package com.buildzone.zonebu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long = 0L,
    val action: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
