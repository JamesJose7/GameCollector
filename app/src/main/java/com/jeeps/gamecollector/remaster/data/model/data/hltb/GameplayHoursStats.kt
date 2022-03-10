package com.jeeps.gamecollector.remaster.data.model.data.hltb

import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats

data class GameplayHoursStats(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var platforms: List<Any> = listOf(),
    var imageUrl: String = "",
    var timeLabels: List<List<String>> = listOf(),
    var gameplayMain: Double = 0.0,
    var gameplayMainExtra: Double = 0.0,
    var gameplayCompletionist: Double = 0.0,
    var similarity: Double = 0.0,
    var searchTerm: String = "",
    var playableOn: List<Any> = listOf()
) {
    constructor(gameHoursStats: GameHoursStats) : this() {
        gameplayMain = gameHoursStats.gameplayMain
        gameplayMainExtra = gameHoursStats.gameplayMainExtra
        gameplayCompletionist = gameHoursStats.gameplayCompletionist
    }
}