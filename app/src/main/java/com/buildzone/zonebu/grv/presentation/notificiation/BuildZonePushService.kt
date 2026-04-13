package com.buildzone.zonebu.grv.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.buildzone.zonebu.BuildZoneActivity
import com.buildzone.zonebu.R
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val BUILD_ZONE_CHANNEL_ID = "build_zone_notifications"
private const val BUILD_ZONE_CHANNEL_NAME = "BuildZone Notifications"
private const val BUILD_ZONE_NOT_TAG = "BuildZone"

class BuildZonePushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                buildZoneShowNotification(it.title ?: BUILD_ZONE_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                buildZoneShowNotification(it.title ?: BUILD_ZONE_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            buildZoneHandleDataPayload(remoteMessage.data)
        }
    }

    private fun buildZoneShowNotification(title: String, message: String, data: String?) {
        val buildZoneNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                BUILD_ZONE_CHANNEL_ID,
                BUILD_ZONE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            buildZoneNotificationManager.createNotificationChannel(channel)
        }

        val buildZoneIntent = Intent(this, BuildZoneActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val buildZonePendingIntent = PendingIntent.getActivity(
            this,
            0,
            buildZoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val buildZoneNotification = NotificationCompat.Builder(this, BUILD_ZONE_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.build_zone_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(buildZonePendingIntent)
            .build()

        buildZoneNotificationManager.notify(System.currentTimeMillis().toInt(), buildZoneNotification)
    }

    private fun buildZoneHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}