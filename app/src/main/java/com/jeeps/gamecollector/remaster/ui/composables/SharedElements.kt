package com.jeeps.gamecollector.remaster.ui.composables

sealed class SharedElements {
    data class GameImage(val gameId: String) : SharedElements()
    data object NavButton : SharedElements()
    data object Fab : SharedElements()
}