package com.jeeps.gamecollector.remaster.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.jeeps.gamecollector.remaster.data.model.data.user.UserStats
import com.jeeps.gamecollector.remaster.data.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@ExperimentalCoroutinesApi
class StatsCollectionDao @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun getUserStats(username: String) : Flow<State<UserStats?>> = callbackFlow {
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
}