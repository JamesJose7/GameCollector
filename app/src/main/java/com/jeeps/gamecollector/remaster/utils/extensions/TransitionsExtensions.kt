package com.jeeps.gamecollector.remaster.utils.extensions

import android.transition.Transition


fun Transition.withExclusions(): Transition {
    return this.apply {
        excludeTarget(android.R.id.statusBarBackground, true)
        excludeTarget(android.R.id.navigationBarBackground, true)
    }
}