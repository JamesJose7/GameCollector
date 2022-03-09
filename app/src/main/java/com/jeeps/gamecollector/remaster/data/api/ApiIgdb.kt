package com.jeeps.gamecollector.remaster.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.igdb.GameCoverIG
import com.jeeps.gamecollector.model.igdb.GameIG
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiIgdb {

    @POST("/v4/games")
    suspend fun searchGames(@Body body: String): NetworkResponse<List<GameIG>, ErrorResponse>

    @POST("/v4/covers")
    suspend fun getImageCoverById(@Body body: String): NetworkResponse<List<GameCoverIG>, ErrorResponse>
}