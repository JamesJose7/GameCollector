package com.jeeps.gamecollector.remaster.data.model

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.data.api.ApiStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatsDao @Inject constructor(
    private val apiStats: ApiStats
) {

    private val dispatcher = Dispatchers.IO

    suspend fun getGameHours(gameName: String): NetworkResponse<GameplayHoursStats, ErrorResponse> {
        return withContext(dispatcher) {
            apiStats.getGameHours(gameName)
        }
    }
}