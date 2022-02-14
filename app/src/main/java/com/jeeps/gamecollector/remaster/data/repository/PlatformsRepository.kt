package com.jeeps.gamecollector.remaster.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.firestore.PlatformsCollectionDao
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import com.jeeps.gamecollector.remaster.data.model.PlatformsDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PlatformsRepository @Inject constructor(
    private val platformsDao: PlatformsDao,
    private val platformsCollectionDao: PlatformsCollectionDao
) {

    suspend fun getPlatforms(username: String) = platformsCollectionDao.getPlatforms(username)

    suspend fun savePlatform(token: String, platform: Platform): NetworkResponse<Platform, ErrorResponse> =
        platformsDao.savePlatform(token, platform)

    suspend fun editPlatform(token: String, platform: Platform): NetworkResponse<Platform, ErrorResponse> =
        platformsDao.editPlatform(token, platform)

    suspend fun uploadPlatformCover(token: String, platformId: String, image: MultipartBody.Part):
            NetworkResponse<ResponseBody, ErrorResponse> =
        platformsDao.uploadImageCover(token, platformId, image)
}