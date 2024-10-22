package com.jeeps.gamecollector.remaster.utils.extensions

import com.jeeps.gamecollector.remaster.data.model.data.user.UserStats
import kotlin.math.roundToInt

fun UserStats.totalGames(): Int {
    return digitalTotal + physicalTotal
}

fun UserStats.completionPercentage(): Int {
    return if (totalGames() > 0) {
        ((completedGamesTotal * 100f) / totalGames()).roundToInt()
    } else {
        0
    }
}

fun UserStats.completionPercent(): Float {
    return if (totalGames() > 0) {
        (completedGamesTotal.toFloat() / totalGames().toFloat())
    } else {
        0f
    }
}