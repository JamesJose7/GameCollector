package com.jeeps.gamecollector.remaster.utils.extensions

fun Boolean?.value(): Boolean {
    if (this == null) {
        return false
    }
    return this
}