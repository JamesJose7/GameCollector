package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.firestore.PlatformsCollectionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PlatformsRepository @Inject constructor(
    private val platformsCollectionDao: PlatformsCollectionDao
) {

    fun getPlatforms() = platformsCollectionDao.getPlatforms("joseeguigurenp")
}