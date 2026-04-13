package com.buildzone.zonebu.grv.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication

class BuildZonePushHandler {
    fun buildZoneHandlePush(extras: Bundle?) {
        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = buildZoneBundleToMap(extras)
            Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    BuildZoneApplication.BUILD_ZONE_FB_LI = map["url"]
                    Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Push data no!")
        }
    }

    private fun buildZoneBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}