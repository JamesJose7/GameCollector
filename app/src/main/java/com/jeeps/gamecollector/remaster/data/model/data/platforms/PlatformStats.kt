package com.jeeps.gamecollector.remaster.data.model.data.platforms

data class PlatformStats(
    var platformId: String = "",
    var platformName: String = "",
    var physicalTotal: Int = 0,
    var digitalTotal: Int = 0,
    var completedGamesTotal: Int = 0,
    var lastGameCompleted: String = ""
)