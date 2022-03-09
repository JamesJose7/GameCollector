package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.Game
import com.jeeps.gamecollector.model.ToggleCompletionResponse
import com.jeeps.gamecollector.remaster.data.api.ApiGame
import com.jeeps.gamecollector.remaster.utils.extensions.bearer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class GamesDao @Inject constructor(
    private val apiGame: ApiGame
) {

    private val dispatcher = Dispatchers.IO

    suspend fun deleteGame(token: String, gameId: String): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiGame.deleteGame("Bearer $token", gameId)
        }
    }

    suspend fun toggleGameCompletion(token: String, gameId: String): NetworkResponse<ToggleCompletionResponse, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiGame.toggleGameCompletion("Bearer $token", gameId)
        }
    }

    suspend fun postGame(token: String, game: Game): NetworkResponse<Game, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiGame.postGame(token.bearer(), game)
        }
    }

    suspend fun editGame(token: String, gameId: String, game: Game): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiGame.editGame(token.bearer(), gameId, game)
        }
    }

    suspend fun uploadGameCover(token: String, gameId: String, body: MultipartBody.Part): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(dispatcher + NonCancellable) {
            apiGame.uploadGameCover(token.bearer(), gameId, body)
        }
    }
}