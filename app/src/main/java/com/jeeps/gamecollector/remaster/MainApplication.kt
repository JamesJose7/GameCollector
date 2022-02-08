package com.jeeps.gamecollector.remaster

import android.app.Application
import com.jeeps.gamecollector.remaster.utils.PreferencesWrapper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferencesWrapper.init(this.applicationContext)
    }
}