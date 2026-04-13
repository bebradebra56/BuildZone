package com.buildzone.zonebu.grv.domain.usecases

import android.util.Log
import com.buildzone.zonebu.grv.data.repo.BuildZoneRepository
import com.buildzone.zonebu.grv.data.utils.BuildZonePushToken
import com.buildzone.zonebu.grv.data.utils.BuildZoneSystemService
import com.buildzone.zonebu.grv.domain.model.BuildZoneEntity
import com.buildzone.zonebu.grv.domain.model.BuildZoneParam
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication

class BuildZoneGetAllUseCase(
    private val buildZoneRepository: BuildZoneRepository,
    private val buildZoneSystemService: BuildZoneSystemService,
    private val buildZonePushToken: BuildZonePushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : BuildZoneEntity?{
        val params = BuildZoneParam(
            buildZoneLocale = buildZoneSystemService.buildZoneGetLocale(),
            buildZonePushToken = buildZonePushToken.buildZoneGetToken(),
            buildZoneAfId = buildZoneSystemService.buildZoneGetAppsflyerId()
        )
        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Params for request: $params")
        return buildZoneRepository.buildZoneGetClient(params, conversion)
    }



}