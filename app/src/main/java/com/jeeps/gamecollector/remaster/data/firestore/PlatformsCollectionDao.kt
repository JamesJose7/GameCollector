package com.jeeps.gamecollector.remaster.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PlatformsCollectionDao @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) {

    fun getPlatforms(username: String) : Flow<State<List<Platform>>> = callbackFlow {
        trySend(State.Loading())

        val userPlatforms = firebaseFirestore.collection("platforms")
            .whereEqualTo("user", username)
            .orderBy("name", Query.Direction.ASCENDING)

        val subscription = userPlatforms
            .addSnapshotListener { snapshot, e ->
                e?.let {
                    trySend(State.Failed(it.message.toString()))
                    cancel(it.message.toString())
                }
                val platforms = mutableListOf<Platform>()
                snapshot?.forEach {
                    val platform: Platform = it.toObject(Platform::class.java).apply {
                        id = it.id
                    }
                    platforms.add(platform)
                }
                trySend(State.Success(platforms))
            }

        awaitClose { subscription.remove() }
    }
}