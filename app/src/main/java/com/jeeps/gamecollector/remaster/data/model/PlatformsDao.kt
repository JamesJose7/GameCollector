package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.api.ApiPlatform
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.utils.extensions.bearer
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class PlatformsDao @Inject constructor(
    private val apiPlatform: ApiPlatform
) {

    suspend fun savePlatform(token: String, platform: Platform): NetworkResponse<Platform, ErrorResponse> {
        return apiPlatform.savePlatform(token.bearer(), platform)
    }

    suspend fun editPlatform(token: String, platform: Platform): NetworkResponse<Platform, ErrorResponse> {
        return apiPlatform.editPlatform(token.bearer(), platform.id, platform)
    }

    suspend fun uploadImageCover(token: String, platformId: String, image: MultipartBody.Part):
            NetworkResponse<ResponseBody, ErrorResponse> {
        return apiPlatform.uploadPlatformCover(token.bearer(), platformId, image)
    }
}