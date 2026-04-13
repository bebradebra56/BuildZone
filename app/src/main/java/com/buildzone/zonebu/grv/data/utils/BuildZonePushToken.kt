package com.buildzone.zonebu.grv.data.utils

import android.util.Log
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class BuildZonePushToken {

    suspend fun buildZoneGetToken(
        buildZoneMaxAttempts: Int = 3,
        buildZoneDelayMs: Long = 1500
    ): String {

        repeat(buildZoneMaxAttempts - 1) {
            try {
                val buildZoneToken = FirebaseMessaging.getInstance().token.await()
                return buildZoneToken
            } catch (e: Exception) {
                Log.e(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(buildZoneDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}