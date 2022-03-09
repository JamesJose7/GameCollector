package com.jeeps.gamecollector.remaster.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.Game
import com.jeeps.gamecollector.model.ToggleCompletionResponse
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiGame {

    @POST("/api/games/{gameId}/delete")
    suspend fun deleteGame(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: String
    ): NetworkResponse<ResponseBody, ErrorResponse>

    @POST("/api/games/toggleCompletion/{gameId}")
    suspend fun toggleGameCompletion(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: String
    ): NetworkResponse<ToggleCompletionResponse, ErrorResponse>

    @POST("/api/games")
    suspend fun postGame(
        @Header("Authorization") authorization: String,
        @Body game: Game
    ): NetworkResponse<Game, ErrorResponse>

    @POST("/api/games/{gameId}")
    suspend fun editGame(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: String,
        @Body game: Game
    ): NetworkResponse<ResponseBody, ErrorResponse>

    @Multipart
    @POST("/api/games/{gameId}/image")
    suspend fun uploadGameCover(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: String,
        @Part body: MultipartBody.Part
    ): NetworkResponse<ResponseBody, ErrorResponse>
}