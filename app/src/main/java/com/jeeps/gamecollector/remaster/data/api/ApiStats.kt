package com.jeeps.gamecollector.remaster.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiStats {

    @GET("/api/getGameHours")
    suspend fun getGameHours(@Query("name") gameName: String):
            NetworkResponse<GameplayHoursStats, ErrorResponse>
}