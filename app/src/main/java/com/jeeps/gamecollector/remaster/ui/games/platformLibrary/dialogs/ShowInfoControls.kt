package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat.*

data class ShowInfoControls(
    var isHoursMain: Boolean = false,
    var isHoursExtra: Boolean = false,
    var isHoursCompletionist: Boolean = false
)

data class ShowInfoData(
    val sortStat: SortStat = NONE
)

fun ShowInfoControls.getInfoData(): ShowInfoData {
    return when {
        isHoursMain -> ShowInfoData(HOURS_MAIN)
        isHoursExtra -> ShowInfoData(HOURS_MAIN_EXTRA)
        isHoursCompletionist -> ShowInfoData(HOURS_COMPLETIONIST)
        else -> ShowInfoData(NONE)
    }
}

fun getInfoControlsFromSortStat(sortStat: SortStat?): ShowInfoControls {
    return if (sortStat != null) {
        when (sortStat) {
            HOURS_MAIN -> ShowInfoControls(isHoursMain = true)
            HOURS_MAIN_EXTRA -> ShowInfoControls(isHoursExtra = true)
            HOURS_COMPLETIONIST -> ShowInfoControls(isHoursCompletionist = true)
            NONE -> ShowInfoControls()
        }
    } else
        ShowInfoControls()
}