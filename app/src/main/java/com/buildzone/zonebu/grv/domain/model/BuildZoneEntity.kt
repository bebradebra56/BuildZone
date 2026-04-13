package com.buildzone.zonebu.grv.domain.model

import com.google.gson.annotations.SerializedName


data class BuildZoneEntity (
    @SerializedName("ok")
    val buildZoneOk: String,
    @SerializedName("url")
    val buildZoneUrl: String,
    @SerializedName("expires")
    val buildZoneExpires: Long,
)