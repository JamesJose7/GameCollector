package com.jeeps.gamecollector.remaster.ui.base

enum class ErrorType(val message: String) {
    SERVER_ERROR("There was a problem in our end, please try again"),
    NETWORK_ERROR("There was a problem with your connection to the internet"),
    UNKNOWN_ERROR("An unknown error has occurred"),
}