package com.buildzone.zonebu.grv.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.grv.data.shar.BuildZoneSharedPreference
import com.buildzone.zonebu.grv.data.utils.BuildZoneSystemService
import com.buildzone.zonebu.grv.domain.usecases.BuildZoneGetAllUseCase
import com.buildzone.zonebu.grv.presentation.app.BuildZoneAppsFlyerState
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuildZoneLoadViewModel(
    private val buildZoneGetAllUseCase: BuildZoneGetAllUseCase,
    private val buildZoneSharedPreference: BuildZoneSharedPreference,
    private val buildZoneSystemService: BuildZoneSystemService
) : ViewModel() {

    private val _buildZoneHomeScreenState: MutableStateFlow<BuildZoneHomeScreenState> =
        MutableStateFlow(BuildZoneHomeScreenState.BuildZoneLoading)
    val buildZoneHomeScreenState = _buildZoneHomeScreenState.asStateFlow()

    private var buildZoneGetApps = false


    init {
        viewModelScope.launch {
            when (buildZoneSharedPreference.buildZoneAppState) {
                0 -> {
                    if (buildZoneSystemService.buildZoneIsOnline()) {
                        BuildZoneApplication.buildZoneConversionFlow.collect {
                            when(it) {
                                BuildZoneAppsFlyerState.BuildZoneDefault -> {}
                                BuildZoneAppsFlyerState.BuildZoneError -> {
                                    buildZoneSharedPreference.buildZoneAppState = 2
                                    _buildZoneHomeScreenState.value =
                                        BuildZoneHomeScreenState.BuildZoneError
                                    buildZoneGetApps = true
                                }
                                is BuildZoneAppsFlyerState.BuildZoneSuccess -> {
                                    if (!buildZoneGetApps) {
                                        buildZoneGetData(it.buildZoneData)
                                        buildZoneGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _buildZoneHomeScreenState.value =
                            BuildZoneHomeScreenState.BuildZoneNotInternet
                    }
                }
                1 -> {
                    if (buildZoneSystemService.buildZoneIsOnline()) {
                        if (BuildZoneApplication.BUILD_ZONE_FB_LI != null) {
                            _buildZoneHomeScreenState.value =
                                BuildZoneHomeScreenState.BuildZoneSuccess(
                                    BuildZoneApplication.BUILD_ZONE_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > buildZoneSharedPreference.buildZoneExpired) {
                            Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Current time more then expired, repeat request")
                            BuildZoneApplication.buildZoneConversionFlow.collect {
                                when(it) {
                                    BuildZoneAppsFlyerState.BuildZoneDefault -> {}
                                    BuildZoneAppsFlyerState.BuildZoneError -> {
                                        _buildZoneHomeScreenState.value =
                                            BuildZoneHomeScreenState.BuildZoneSuccess(
                                                buildZoneSharedPreference.buildZoneSavedUrl
                                            )
                                        buildZoneGetApps = true
                                    }
                                    is BuildZoneAppsFlyerState.BuildZoneSuccess -> {
                                        if (!buildZoneGetApps) {
                                            buildZoneGetData(it.buildZoneData)
                                            buildZoneGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Current time less then expired, use saved url")
                            _buildZoneHomeScreenState.value =
                                BuildZoneHomeScreenState.BuildZoneSuccess(
                                    buildZoneSharedPreference.buildZoneSavedUrl
                                )
                        }
                    } else {
                        _buildZoneHomeScreenState.value =
                            BuildZoneHomeScreenState.BuildZoneNotInternet
                    }
                }
                2 -> {
                    _buildZoneHomeScreenState.value =
                        BuildZoneHomeScreenState.BuildZoneError
                }
            }
        }
    }


    private suspend fun buildZoneGetData(conversation: MutableMap<String, Any>?) {
        val buildZoneData = buildZoneGetAllUseCase.invoke(conversation)
        if (buildZoneSharedPreference.buildZoneAppState == 0) {
            if (buildZoneData == null) {
                buildZoneSharedPreference.buildZoneAppState = 2
                _buildZoneHomeScreenState.value =
                    BuildZoneHomeScreenState.BuildZoneError
            } else {
                buildZoneSharedPreference.buildZoneAppState = 1
                buildZoneSharedPreference.apply {
                    buildZoneExpired = buildZoneData.buildZoneExpires
                    buildZoneSavedUrl = buildZoneData.buildZoneUrl
                }
                _buildZoneHomeScreenState.value =
                    BuildZoneHomeScreenState.BuildZoneSuccess(buildZoneData.buildZoneUrl)
            }
        } else  {
            if (buildZoneData == null) {
                _buildZoneHomeScreenState.value =
                    BuildZoneHomeScreenState.BuildZoneSuccess(
                        buildZoneSharedPreference.buildZoneSavedUrl
                    )
            } else {
                buildZoneSharedPreference.apply {
                    buildZoneExpired = buildZoneData.buildZoneExpires
                    buildZoneSavedUrl = buildZoneData.buildZoneUrl
                }
                _buildZoneHomeScreenState.value =
                    BuildZoneHomeScreenState.BuildZoneSuccess(buildZoneData.buildZoneUrl)
            }
        }
    }


    sealed class BuildZoneHomeScreenState {
        data object BuildZoneLoading : BuildZoneHomeScreenState()
        data object BuildZoneError : BuildZoneHomeScreenState()
        data class BuildZoneSuccess(val data: String) : BuildZoneHomeScreenState()
        data object BuildZoneNotInternet: BuildZoneHomeScreenState()
    }
}