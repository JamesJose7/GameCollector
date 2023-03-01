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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameHoursStats

        if (gameplayMain != other.gameplayMain) return false
        if (gameplayMainExtra != other.gameplayMainExtra) return false
        if (gameplayCompletionist != other.gameplayCompletionist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameplayMain.hashCode()
        result = 31 * result + gameplayMainExtra.hashCode()
        result = 31 * result + gameplayCompletionist.hashCode()
        return result
    }

}