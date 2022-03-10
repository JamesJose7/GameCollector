package com.jeeps.gamecollector.remaster.data.model.data.games

import java.io.Serializable

data class ToggleCompletionResponse(
    var isCompleted: Boolean = false
) : Serializable