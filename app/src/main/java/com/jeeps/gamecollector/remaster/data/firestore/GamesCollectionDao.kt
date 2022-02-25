package com.jeeps.gamecollector.remaster.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jeeps.gamecollector.model.Game
import com.jeeps.gamecollector.model.GameHoursStats
import com.jeeps.gamecollector.model.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.data.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
class GamesCollectionDao @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun getUserGamesByPlatform(username: String, platformId: String) : Flow<State<List<Game>?>> = callbackFlow {
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

    suspend fun updateGameHours(stats: GameplayHoursStats, gameId: String): State<Boolean> {
        return suspendCoroutine { continuation ->
            val gameRef = firebaseFirestore
                .collection("games")
                .document(gameId)

            gameRef.update("gameHoursStats", GameHoursStats(stats))
                .addOnCompleteListener { continuation.resume(State.Success(true)) }
                .addOnFailureListener { continuation.resume(State.Failed(it.message ?: "", it)) }
        }
    }
}