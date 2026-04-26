package com.jeeps.gamecollector.remaster.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.api.ApiStats
import com.jeeps.gamecollector.remaster.data.model.data.user.UserStats
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserStatsRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val apiStats: ApiStats
) {

    fun getUserStats(username: String) : Flow<State<UserStats?>> = callbackFlow {
        trySend(State.Loading())

        val userStatsRef = firebaseFirestore
            .collection("stats")
            .whereEqualTo("user", username)
            .limit(1)

        val subscription = userStatsRef
            .addSnapshotListener { snapshot, e ->
                e?.let {
                    trySend(State.Failed(it.message.toString(), e))
                    cancel(it.message.toString())
                }
                val stats = snapshot?.documents?.get(0)?.toObject(UserStats::class.java)
                trySend(State.Success(stats))
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getGameHours(gameName: String) = apiStats.getGameHours(gameName)
}