package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

data class SortControls(
    var isAscending: Boolean = false,
    var isPhysical: Boolean = false,
    var isDigital: Boolean = false,
    var isAlphabetical: Boolean = false,
    var isCompletion: Boolean = false,
    var isHoursMain: Boolean = false,
    var isHoursExtra: Boolean = false,
    var isHoursCompletionist: Boolean = false
)
