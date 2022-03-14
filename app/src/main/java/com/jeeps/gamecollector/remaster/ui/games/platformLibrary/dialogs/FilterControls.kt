package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import com.jeeps.gamecollector.remaster.data.model.data.games.Game

data class FilterControls(
    var completed: Boolean = false,
    var notCompleted: Boolean = false
)

data class FilterData(
    val filtersList: List<(Game) -> Boolean>
)

fun FilterControls.getFilterData(): FilterData {
    val predicates = mutableListOf<(Game) -> Boolean>()
    if (completed)
        predicates.add { game -> game.timesCompleted > 0 }
    if (notCompleted)
        predicates.add { game -> game.timesCompleted == 0 }
    return FilterData(predicates)
}

fun FilterControls.isNotCleared(): Boolean {
    val controls = listOf(completed, notCompleted)
    return controls.any { control -> control }
}