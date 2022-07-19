package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.api.ApiStats
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import javax.inject.Inject

class StatsDao @Inject constructor(
    private val apiStats: ApiStats
) {

    suspend fun getGameHours(gameName: String): NetworkResponse<GameplayHoursStats, ErrorResponse> {
        return apiStats.getGameHours(gameName)
    }
}