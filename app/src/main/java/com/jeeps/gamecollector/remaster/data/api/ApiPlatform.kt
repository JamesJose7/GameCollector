package com.jeeps.gamecollector.remaster.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiPlatform {

    @POST("/api/platforms")
    suspend fun savePlatform(
        @Header("Authorization") authorization: String,
        @Body platform: Platform
    ): NetworkResponse<Platform, ErrorResponse>

    @POST("/api/platforms/{platformId}")
    suspend fun editPlatform(
        @Header("Authorization") authorization: String,
        @Path("platformId") platformId: String,
        @Body platform: Platform
    ): NetworkResponse<Platform, ErrorResponse>

    @Multipart
    @POST("/api/platforms/{platformId}/image")
    suspend fun uploadPlatformCover(
        @Header("Authorization") authorization: String,
        @Path("platformId") platformId: String,
        @Part image: MultipartBody.Part
    ): NetworkResponse<ResponseBody, ErrorResponse>
}