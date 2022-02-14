package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.api.ApiPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class PlatformsDao @Inject constructor(
    private val apiPlatform: ApiPlatform
) {

    private val dispatcher = Dispatchers.IO

    suspend fun savePlatform(token: String, platform: Platform): NetworkResponse<Platform, ErrorResponse> {
        return withContext(dispatcher) {
            apiPlatform.savePlatform("Bearer $token", platform)
        }
    }

    suspend fun editPlatform(token: String, platform: Platform): NetworkResponse<Platform, ErrorResponse> {
        return withContext(dispatcher) {
            apiPlatform.editPlatform("Bearer $token", platform.id, platform)
        }
    }

    suspend fun uploadImageCover(token: String, platformId: String, image: MultipartBody.Part):
            NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(dispatcher) {
            apiPlatform.uploadPlatformCover("Bearer $token", platformId, image)
        }
    }
}