package com.jeeps.gamecollector.remaster.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.api.ApiPlatform
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.utils.extensions.bearer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PlatformsRepository @Inject constructor(
    private val apiPlatform: ApiPlatform,
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun getPlatforms(username: String): Flow<State<List<Platform>>> = callbackFlow {
        trySend(State.Loading())

        val userPlatforms = firebaseFirestore.collection("platforms")
            .whereEqualTo("user", username)
            .orderBy("name", Query.Direction.ASCENDING)

        val subscription = userPlatforms
            .addSnapshotListener { snapshot, e ->
                e?.let {
                    trySend(State.Failed(it.message.toString(), e))
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

    suspend fun savePlatform(
        token: String,
        platform: Platform
    ): NetworkResponse<Platform, ErrorResponse> =
        apiPlatform.savePlatform(token.bearer(), platform)

    suspend fun editPlatform(
        token: String,
        platform: Platform
    ): NetworkResponse<Platform, ErrorResponse> =
        apiPlatform.editPlatform(token.bearer(), platform.id, platform)

    suspend fun uploadPlatformCover(
        token: String,
        platformId: String,
        image: MultipartBody.Part
    ): NetworkResponse<ResponseBody, ErrorResponse> =
        apiPlatform.uploadPlatformCover(token.bearer(), platformId, image)
}