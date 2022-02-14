package com.jeeps.gamecollector.remaster.ui.games

import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GamesFromPlatformViewModel @Inject constructor(

) : BaseViewModel() {

    var platformId: String = ""
    var platformName: String = ""
}