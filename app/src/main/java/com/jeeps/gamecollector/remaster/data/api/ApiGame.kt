package com.jeeps.gamecollector.remaster.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiGame {

    @POST("/api/games/{gameId}/delete")
    suspend fun deleteGame(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: String
    ): NetworkResponse<ResponseBody, ErrorResponse>
}