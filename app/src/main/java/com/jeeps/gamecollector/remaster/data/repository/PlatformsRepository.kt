package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.firestore.PlatformsCollectionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PlatformsRepository @Inject constructor(
    private val platformsCollectionDao: PlatformsCollectionDao
) {

    suspend fun getPlatforms(username: String) = platformsCollectionDao.getPlatforms(username)
}