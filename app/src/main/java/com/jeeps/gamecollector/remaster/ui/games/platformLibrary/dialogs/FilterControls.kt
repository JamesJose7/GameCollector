package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import com.jeeps.gamecollector.remaster.data.model.data.games.Game

data class FilterControls(
    var completed: Boolean = false,
    var notCompleted: Boolean = false,
    var isDigital: Boolean = false,
    var isPhysical: Boolean = false
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
    if (isDigital)
        predicates.add { game -> !game.isPhysical }
    if (isPhysical)
        predicates.add { game -> game.isPhysical }
    return FilterData(predicates)
}

fun FilterControls.isNotCleared(): Boolean {
    val controls = listOf(completed, notCompleted, isDigital, isPhysical)
    return controls.any { control -> control }
}