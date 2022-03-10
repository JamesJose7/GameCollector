package com.jeeps.gamecollector.remaster.utils.extensions

import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats
import kotlin.math.roundToInt

fun PlatformStats.totalGames(): Int {
    return digitalTotal + physicalTotal
}

fun PlatformStats.completionPercentage(): Int {
    return if (totalGames() > 0) {
        ((completedGamesTotal * 100f) / totalGames()).roundToInt()
    } else {
        0
    }
}