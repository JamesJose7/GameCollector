package com.jeeps.gamecollector.remaster.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.api.ApiIgdb
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import com.jeeps.gamecollector.remaster.data.model.data.igdb.GameCoverIG
import com.jeeps.gamecollector.remaster.data.model.data.igdb.GameIG
import com.jeeps.gamecollector.remaster.data.model.data.igdb.GenreIg
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IgdbRepository @Inject constructor(
    private val apiIgdb: ApiIgdb
) {

    suspend fun searchGames(query: String): NetworkResponse<List<GameIG>, ErrorResponse> {
        return withContext(NonCancellable) {
            apiIgdb.searchGames(query)
        }
    }

    suspend fun getGameCoverById(gameId: String): NetworkResponse<List<GameCoverIG>, ErrorResponse> {
        return withContext(NonCancellable) {
            apiIgdb.getImageCoverById(gameId)
        }
    }

    suspend fun getGenresByIds(gameId: String): NetworkResponse<List<GenreIg>, ErrorResponse> {
        return withContext(NonCancellable) {
            apiIgdb.getGenresByIds(gameId)
        }
    }
}