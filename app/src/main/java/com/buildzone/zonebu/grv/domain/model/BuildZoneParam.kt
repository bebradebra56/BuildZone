package com.buildzone.zonebu.grv.domain.model

import com.google.gson.annotations.SerializedName


private const val BUILD_ZONE_A = "com.buildzone.zonebu"
private const val BUILD_ZONE_B = "buildzone-3c891"
data class BuildZoneParam (
    @SerializedName("af_id")
    val buildZoneAfId: String,
    @SerializedName("bundle_id")
    val buildZoneBundleId: String = BUILD_ZONE_A,
    @SerializedName("os")
    val buildZoneOs: String = "Android",
    @SerializedName("store_id")
    val buildZoneStoreId: String = BUILD_ZONE_A,
    @SerializedName("locale")
    val buildZoneLocale: String,
    @SerializedName("push_token")
    val buildZonePushToken: String,
    @SerializedName("firebase_project_id")
    val buildZoneFirebaseProjectId: String = BUILD_ZONE_B,

    )