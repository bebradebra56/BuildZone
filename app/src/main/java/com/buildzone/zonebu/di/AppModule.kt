package com.buildzone.zonebu.di

import androidx.room.Room
import com.buildzone.zonebu.data.datastore.SettingsDataStore
import com.buildzone.zonebu.data.db.AppDatabase
import com.buildzone.zonebu.data.repository.*
import com.buildzone.zonebu.presentation.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "buildzone.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single { get<AppDatabase>().projectDao() }
    single { get<AppDatabase>().roomDao() }
    single { get<AppDatabase>().measurementDao() }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().activityLogDao() }

    single { ProjectRepository(get(), get()) }
    single { RoomRepository(get(), get()) }
    single { MeasurementRepository(get(), get()) }
    single { TaskRepository(get(), get()) }
    single { ActivityLogRepository(get()) }

    single { SettingsDataStore(androidContext()) }

    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { ProjectViewModel(get()) }
    viewModel { (projectId: Long) -> RoomViewModel(get(), projectId) }
    viewModel { (roomId: Long) -> MeasurementViewModel(get(), roomId) }
    viewModel { (roomId: Long) -> HeatMapViewModel(get(), roomId) }
    viewModel { TaskViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { ActivityViewModel(get()) }
    viewModel { ReportsViewModel(get(), get(), get()) }
}
