package com.jeeps.gamecollector.remaster.utils.comparators

import com.jeeps.gamecollector.remaster.data.model.data.games.Game

/**
 * Created by jeeps on 1/9/2018.
 */
class GameByTimesPlayedComparator : Comparator<Game> {
    private var desc: Boolean

    constructor(desc: Boolean) {
        this.desc = desc
    }

    constructor() {
        desc = false
    }

    override fun compare(game1: Game, game2: Game): Int {
        return if (!desc)
            game1.timesCompleted.compareTo(game2.timesCompleted)
        else
            game1.timesCompleted.compareTo(game2.timesCompleted) * -1
    }
}