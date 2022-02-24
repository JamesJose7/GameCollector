package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.api.ApiGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import javax.inject.Inject

class GamesDao @Inject constructor(
    private val apiGame: ApiGame
) {

    private val dispatcher = Dispatchers.IO

    suspend fun deleteGame(token: String, gameId: String): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(dispatcher) {
            apiGame.deleteGame("Bearer $token", gameId)
        }
    }
}