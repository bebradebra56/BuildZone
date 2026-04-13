package com.buildzone.zonebu.grv.data.repo

import android.util.Log
import com.buildzone.zonebu.grv.domain.model.BuildZoneEntity
import com.buildzone.zonebu.grv.domain.model.BuildZoneParam
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication.Companion.BUILD_ZONE_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface BuildZoneApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun buildZoneGetClient(
        @Body jsonString: JsonObject,
    ): Call<BuildZoneEntity>
}


private const val BUILD_ZONE_MAIN = "https://builldzone.com/"
class BuildZoneRepository {

    suspend fun buildZoneGetClient(
        buildZoneParam: BuildZoneParam,
        buildZoneConversion: MutableMap<String, Any>?
    ): BuildZoneEntity? {
        val gson = Gson()
        val api = buildZoneGetApi(BUILD_ZONE_MAIN, null)

        val buildZoneJsonObject = gson.toJsonTree(buildZoneParam).asJsonObject
        buildZoneConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            buildZoneJsonObject.add(key, element)
        }
        return try {
            val buildZoneRequest: Call<BuildZoneEntity> = api.buildZoneGetClient(
                jsonString = buildZoneJsonObject,
            )
            val buildZoneResult = buildZoneRequest.awaitResponse()
            Log.d(BUILD_ZONE_MAIN_TAG, "Retrofit: Result code: ${buildZoneResult.code()}")
            if (buildZoneResult.code() == 200) {
                Log.d(BUILD_ZONE_MAIN_TAG, "Retrofit: Get request success")
                Log.d(BUILD_ZONE_MAIN_TAG, "Retrofit: Code = ${buildZoneResult.code()}")
                Log.d(BUILD_ZONE_MAIN_TAG, "Retrofit: ${buildZoneResult.body()}")
                buildZoneResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(BUILD_ZONE_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(BUILD_ZONE_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun buildZoneGetApi(url: String, client: OkHttpClient?) : BuildZoneApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
