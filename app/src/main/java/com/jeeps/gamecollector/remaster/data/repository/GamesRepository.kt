package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.firestore.GamesCollectionDao
import com.jeeps.gamecollector.remaster.data.model.GamesDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class GamesRepository @Inject constructor(
    private val gamesCollectionDao: GamesCollectionDao,
    private val gamesDao: GamesDao
) {

    suspend fun getUserGamesByPlatform(username: String, platformId: String) =
        gamesCollectionDao.getUserGamesByPlatform(username, platformId)

    suspend fun deleteGame(token: String, gameId: String) =
        gamesDao.deleteGame(token, gameId)
}