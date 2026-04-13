package com.buildzone.zonebu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildzone.zonebu.data.datastore.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsDataStore: SettingsDataStore) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch { settingsDataStore.setTemperatureUnit(unit) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    }

    fun setLengthUnit(unit: LengthUnit) {
        viewModelScope.launch { settingsDataStore.setLengthUnit(unit) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { settingsDataStore.setUserName(name) }
    }

    fun setOnboardingDone() {
        viewModelScope.launch { settingsDataStore.setOnboardingDone() }
    }
}
