package com.jeeps.gamecollector.remaster.utils.comparators

import com.jeeps.gamecollector.remaster.data.model.data.games.Game

class GameByHoursStoryComparator : Comparator<Game> {
    private val desc: Boolean

    constructor(desc: Boolean) {
        this.desc = desc
    }

    constructor() {
        desc = false
    }

    override fun compare(game1: Game, game2: Game): Int {
        val (hours1) = game1.gameHoursStats
        val (hours2) = game2.gameHoursStats
        return if (!desc)
            hours1.compareTo(hours2)
        else
            hours1.compareTo(hours2) * -1
    }
}