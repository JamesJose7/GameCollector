package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.api.ApiGame
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.ToggleCompletionResponse
import com.jeeps.gamecollector.remaster.utils.extensions.bearer
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class GamesDao @Inject constructor(
    private val apiGame: ApiGame
) {

    suspend fun deleteGame(token: String, gameId: String): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(NonCancellable) {
            apiGame.deleteGame(token.bearer(), gameId)
        }
    }

    suspend fun toggleGameCompletion(token: String, gameId: String): NetworkResponse<ToggleCompletionResponse, ErrorResponse> {
        return withContext(NonCancellable) {
            apiGame.toggleGameCompletion(token.bearer(), gameId)
        }
    }

    suspend fun postGame(token: String, game: Game): NetworkResponse<Game, ErrorResponse> {
        return withContext(NonCancellable) {
            apiGame.postGame(token.bearer(), game)
        }
    }

    suspend fun editGame(token: String, gameId: String, game: Game): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(NonCancellable) {
            apiGame.editGame(token.bearer(), gameId, game)
        }
    }

    suspend fun uploadGameCover(token: String, gameId: String, body: MultipartBody.Part): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(NonCancellable) {
            apiGame.uploadGameCover(token.bearer(), gameId, body)
        }
    }
}