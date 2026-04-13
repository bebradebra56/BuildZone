package com.buildzone.zonebu.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "buildzone_settings")

data class AppSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lengthUnit: LengthUnit = LengthUnit.METRIC,
    val userName: String = "",
    val isOnboardingDone: Boolean = false
)

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class LengthUnit { METRIC, IMPERIAL }

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LENGTH_UNIT = stringPreferencesKey("length_unit")
        val USER_NAME = stringPreferencesKey("user_name")
        val IS_ONBOARDING_DONE = booleanPreferencesKey("is_onboarding_done")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            AppSettings(
                temperatureUnit = TemperatureUnit.entries.find {
                    it.name == prefs[Keys.TEMPERATURE_UNIT]
                } ?: TemperatureUnit.CELSIUS,
                themeMode = ThemeMode.entries.find {
                    it.name == prefs[Keys.THEME_MODE]
                } ?: ThemeMode.SYSTEM,
                lengthUnit = LengthUnit.entries.find {
                    it.name == prefs[Keys.LENGTH_UNIT]
                } ?: LengthUnit.METRIC,
                userName = prefs[Keys.USER_NAME] ?: "",
                isOnboardingDone = prefs[Keys.IS_ONBOARDING_DONE] ?: false
            )
        }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { it[Keys.TEMPERATURE_UNIT] = unit.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLengthUnit(unit: LengthUnit) {
        context.dataStore.edit { it[Keys.LENGTH_UNIT] = unit.name }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[Keys.IS_ONBOARDING_DONE] = true }
    }
}
