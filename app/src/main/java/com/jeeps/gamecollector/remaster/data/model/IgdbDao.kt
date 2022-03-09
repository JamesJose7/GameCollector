package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.igdb.GameCoverIG
import com.jeeps.gamecollector.model.igdb.GameIG
import com.jeeps.gamecollector.remaster.data.api.ApiIgdb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IgdbDao @Inject constructor(
    private val apiIgdb: ApiIgdb
) {

    private val dispatcher = Dispatchers.IO

    suspend fun searchGames(query: String): NetworkResponse<List<GameIG>, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiIgdb.searchGames(query)
        }
    }

    suspend fun getGameCoverById(gameId: String): NetworkResponse<List<GameCoverIG>, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiIgdb.getImageCoverById(gameId)
        }
    }
}