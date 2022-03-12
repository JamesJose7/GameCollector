package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.utils.comparators.*

data class SortControls(
    var isAscending: Boolean = true,
    var isPhysical: Boolean = false,
    var isDigital: Boolean = false,
    var isAlphabetical: Boolean = false,
    var isCompletion: Boolean = false,
    var isHoursMain: Boolean = false,
    var isHoursExtra: Boolean = false,
    var isHoursCompletionist: Boolean = false
)

data class SortData(
    val comparator: Comparator<Game>,
    val sortStat: SortStat = SortStat.NONE
)

fun SortControls.getAppropriateComparator(): SortData {
    return when {
        isDigital -> SortData(GameByPhysicalComparator())
        isPhysical -> SortData(GameByPhysicalComparator(true))
        isAlphabetical -> SortData(GameByNameComparator(!isAscending))
        isCompletion -> SortData(GameByTimesPlayedComparator(!isAscending))
        isHoursMain -> SortData(GameByHoursStoryComparator(!isAscending), SortStat.HOURS_MAIN)
        isHoursExtra -> SortData(GameByHoursMainExtraComparator(!isAscending), SortStat.HOURS_MAIN_EXTRA)
        isHoursCompletionist -> SortData(GameByHoursCompletionistComparator(!isAscending), SortStat.HOURS_COMPLETIONIST)
        else -> SortData(GameByNameComparator())
    }
}
