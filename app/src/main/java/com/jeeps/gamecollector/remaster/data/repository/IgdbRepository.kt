package com.jeeps.gamecollector.remaster.data.repository

import com.jeeps.gamecollector.remaster.data.model.IgdbDao
import javax.inject.Inject

class IgdbRepository @Inject constructor(
    private val igdbDao: IgdbDao
) {

    suspend fun searchGames(query: String) =
        igdbDao.searchGames(query)

    suspend fun getGameCoverById(gameId: String) =
        igdbDao.getGameCoverById(gameId)
}