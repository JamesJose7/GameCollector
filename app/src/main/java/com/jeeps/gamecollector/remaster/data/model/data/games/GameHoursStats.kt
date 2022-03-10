package com.jeeps.gamecollector.remaster.data.model.data.games

import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import java.io.Serializable

data class GameHoursStats(
    var gameplayMain: Double = 0.0,
    var gameplayMainExtra: Double = 0.0,
    var gameplayCompletionist: Double = 0.0
) : Serializable {
    constructor(gameplayHoursStats: GameplayHoursStats) : this(
        gameplayHoursStats.gameplayMain,
        gameplayHoursStats.gameplayMainExtra,
        gameplayHoursStats.gameplayCompletionist)
}