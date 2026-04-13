package com.buildzone.zonebu.grv.presentation.di

import com.buildzone.zonebu.grv.data.repo.BuildZoneRepository
import com.buildzone.zonebu.grv.data.shar.BuildZoneSharedPreference
import com.buildzone.zonebu.grv.data.utils.BuildZonePushToken
import com.buildzone.zonebu.grv.data.utils.BuildZoneSystemService
import com.buildzone.zonebu.grv.domain.usecases.BuildZoneGetAllUseCase
import com.buildzone.zonebu.grv.presentation.pushhandler.BuildZonePushHandler
import com.buildzone.zonebu.grv.presentation.ui.load.BuildZoneLoadViewModel
import com.buildzone.zonebu.grv.presentation.ui.view.BuildZoneViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val buildZoneModule = module {
    factory {
        BuildZonePushHandler()
    }
    single {
        BuildZoneRepository()
    }
    single {
        BuildZoneSharedPreference(get())
    }
    factory {
        BuildZonePushToken()
    }
    factory {
        BuildZoneSystemService(get())
    }
    factory {
        BuildZoneGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        BuildZoneViFun(get())
    }
    viewModel {
        BuildZoneLoadViewModel(get(), get(), get())
    }
}