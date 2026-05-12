package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat

data class ShowInfoControls(
    var isHoursMain: Boolean = false,
    var isHoursExtra: Boolean = false,
    var isHoursCompletionist: Boolean = false
)

enum class ShowStat {
    HoursMain,
    HoursMainExtra,
    HoursCompletionist,
    None
}

fun ShowInfoControls.getShowStat(): ShowStat {
    return when {
        isHoursMain -> ShowStat.HoursMain
        isHoursExtra -> ShowStat.HoursMainExtra
        isHoursCompletionist -> ShowStat.HoursCompletionist
        else -> ShowStat.None
    }
}

fun SortStat.getShowStat(): ShowStat = when(this) {
    SortStat.HOURS_MAIN -> ShowStat.HoursMain
    SortStat.HOURS_MAIN_EXTRA -> ShowStat.HoursMainExtra
    SortStat.HOURS_COMPLETIONIST -> ShowStat.HoursCompletionist
    SortStat.NONE -> ShowStat.None
}