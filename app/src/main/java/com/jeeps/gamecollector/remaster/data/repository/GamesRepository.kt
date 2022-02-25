package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.model.hltb.GameplayHoursStats
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

    suspend fun toggleGameCompletion(token: String, gameId: String) =
        gamesDao.toggleGameCompletion(token, gameId)

    suspend fun updateGameHours(gameplayHoursStats: GameplayHoursStats, gameId: String) =
        gamesCollectionDao.updateGameHours(gameplayHoursStats, gameId)
}