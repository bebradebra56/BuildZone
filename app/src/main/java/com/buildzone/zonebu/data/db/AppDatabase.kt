package com.buildzone.zonebu.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.buildzone.zonebu.data.db.dao.*
import com.buildzone.zonebu.data.db.entity.*

@Database(
    entities = [
        ProjectEntity::class,
        RoomEntity::class,
        MeasurementEntity::class,
        TaskEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun roomDao(): RoomDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun taskDao(): TaskDao
    abstract fun activityLogDao(): ActivityLogDao
}
