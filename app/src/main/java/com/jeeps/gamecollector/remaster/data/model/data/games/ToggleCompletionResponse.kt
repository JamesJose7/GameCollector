package com.jeeps.gamecollector.remaster.data.model.data.games

import java.io.Serializable

data class ToggleCompletionResponse(
    @set:JvmName("setCompleted")
    @get:JvmName("isCompleted")
    var completed: Boolean = false
) : Serializable