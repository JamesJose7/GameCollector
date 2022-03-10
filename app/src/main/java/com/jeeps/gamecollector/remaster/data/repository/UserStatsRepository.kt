package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.firestore.StatsCollectionDao
import com.jeeps.gamecollector.remaster.data.model.StatsDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserStatsRepository @Inject constructor(
    private val statsCollectionDao: StatsCollectionDao,
    private val statsDao: StatsDao
) {

    suspend fun getUserStats(username: String) = statsCollectionDao.getUserStats(username)

    suspend fun getGameHours(gameName: String) = statsDao.getGameHours(gameName)
}