package com.jeeps.gamecollector.remaster.data.model.data.games

import com.jeeps.gamecollector.model.hltb.GameplayHoursStats
import java.io.Serializable
//
//class GameHoursStats : Serializable {
//    var gameplayMain = 0.0
//    var gameplayMainExtra = 0.0
//    var gameplayCompletionist = 0.0
//
//    constructor(gameplayMain: Double, gameplayMainExtra: Double, gameplayCompletionist: Double) {
//        this.gameplayMain = gameplayMain
//        this.gameplayMainExtra = gameplayMainExtra
//        this.gameplayCompletionist = gameplayCompletionist
//    }
//
//    constructor(gameplayHoursStats: GameplayHoursStats) {
//        gameplayMain = gameplayHoursStats.gameplayMain
//        gameplayMainExtra = gameplayHoursStats.gameplayMainExtra
//        gameplayCompletionist = gameplayHoursStats.gameplayCompletionist
//    }
//
//    constructor() {}
//}

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