package com.buildzone.zonebu.grv.data.shar

import android.content.Context
import androidx.core.content.edit

class BuildZoneSharedPreference(context: Context) {
    private val buildZonePrefs = context.getSharedPreferences("buildZoneSharedPrefsAb", Context.MODE_PRIVATE)

    var buildZoneSavedUrl: String
        get() = buildZonePrefs.getString(BUILD_ZONE_SAVED_URL, "") ?: ""
        set(value) = buildZonePrefs.edit { putString(BUILD_ZONE_SAVED_URL, value) }

    var buildZoneExpired : Long
        get() = buildZonePrefs.getLong(BUILD_ZONE_EXPIRED, 0L)
        set(value) = buildZonePrefs.edit { putLong(BUILD_ZONE_EXPIRED, value) }

    var buildZoneAppState: Int
        get() = buildZonePrefs.getInt(BUILD_ZONE_APPLICATION_STATE, 0)
        set(value) = buildZonePrefs.edit { putInt(BUILD_ZONE_APPLICATION_STATE, value) }

    var buildZoneNotificationRequest: Long
        get() = buildZonePrefs.getLong(BUILD_ZONE_NOTIFICAITON_REQUEST, 0L)
        set(value) = buildZonePrefs.edit { putLong(BUILD_ZONE_NOTIFICAITON_REQUEST, value) }


    var buildZoneNotificationState:Int
        get() = buildZonePrefs.getInt(BUILD_ZONE_NOTIFICATION_STATE, 0)
        set(value) = buildZonePrefs.edit { putInt(BUILD_ZONE_NOTIFICATION_STATE, value) }

    companion object {
        private const val BUILD_ZONE_NOTIFICATION_STATE = "buildZoneNotificationState"
        private const val BUILD_ZONE_SAVED_URL = "buildZoneSavedUrl"
        private const val BUILD_ZONE_EXPIRED = "buildZoneExpired"
        private const val BUILD_ZONE_APPLICATION_STATE = "buildZoneApplicationState"
        private const val BUILD_ZONE_NOTIFICAITON_REQUEST = "buildZoneNotificationRequest"
    }
}