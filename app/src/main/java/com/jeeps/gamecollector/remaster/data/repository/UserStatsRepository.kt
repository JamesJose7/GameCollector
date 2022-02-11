package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.firestore.StatsCollectionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserStatsRepository @Inject constructor(
    private val statsCollectionDao: StatsCollectionDao
) {

    suspend fun getUserStats(username: String) = statsCollectionDao.getUserStats(username)
}