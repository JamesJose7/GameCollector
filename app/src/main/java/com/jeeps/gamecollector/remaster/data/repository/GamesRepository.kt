package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.firestore.GamesCollectionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class GamesRepository @Inject constructor(
    private val gamesCollectionDao: GamesCollectionDao
) {

    suspend fun getUserGamesByPlatform(username: String, platformId: String) =
        gamesCollectionDao.getUserGamesByPlatform(username, platformId)
}