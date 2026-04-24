package com.jeeps.gamecollector.remaster.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.api.ApiGame
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.games.ToggleCompletionResponse
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.utils.extensions.bearer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
class GamesRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val apiGame: ApiGame,
    private val authenticationRepository: AuthenticationRepository
) {

    suspend fun getUserGamesByPlatform(username: String, platformId: String) = callbackFlow {
        trySend(State.Loading())

        val userGamesRef = firebaseFirestore
            .collection("games")
            .whereEqualTo("user", username)
            .whereEqualTo("platformId", platformId)
            .orderBy("name", Query.Direction.ASCENDING)

        val subscription = userGamesRef
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    trySend(State.Failed(it.message.toString(), error))
                    cancel(it.message.toString())
                }
                val games = mutableListOf<Game>()
                snapshot?.forEach {
                    val game: Game = it.toObject(Game::class.java).apply {
                        id = it.id
                    }
                    games.add(game)
                }
                trySend(State.Success(games))
            }

        awaitClose { subscription.remove() }
    }

    suspend fun deleteGame(
        gameId: String
    ): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(NonCancellable) {
            val token = authenticationRepository.getUserToken()
            apiGame.deleteGame(token.bearer(), gameId)
        }
    }

    suspend fun toggleGameCompletion(
        gameId: String
    ): NetworkResponse<ToggleCompletionResponse, ErrorResponse> {
        return withContext(NonCancellable) {
            val token = authenticationRepository.getUserToken()
            apiGame.toggleGameCompletion(token.bearer(), gameId)
        }
    }

    suspend fun saveNewGame(
        game: Game
    ): NetworkResponse<Game, ErrorResponse> {
        val token = authenticationRepository.getUserToken()
        return apiGame.postGame(token.bearer(), game)
    }

    suspend fun editGame(
        gameId: String,
        game: Game
    ): NetworkResponse<ResponseBody, ErrorResponse> {
        val token = authenticationRepository.getUserToken()
        return apiGame.editGame(token.bearer(), gameId, game)
    }

    suspend fun uploadGameCover(
        gameId: String,
        body: MultipartBody.Part
    ): NetworkResponse<ResponseBody, ErrorResponse> {
        val token = authenticationRepository.getUserToken()
        return apiGame.uploadGameCover(token.bearer(), gameId, body)
    }

    suspend fun updateGameHours(
        stats: GameplayHoursStats,
        gameId: String
    ): State<Boolean> {
        return suspendCancellableCoroutine { continuation ->
            val gameRef = firebaseFirestore
                .collection("games")
                .document(gameId)

            gameRef.update("gameHoursStats", GameHoursStats(stats))
                .addOnCompleteListener { continuation.resume(State.Success(true)) }
                .addOnFailureListener { continuation.resume(State.Failed(it.message ?: "", it)) }
        }
    }
}