package com.jeeps.gamecollector.remaster.data.model.data.user

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats

data class UserStats(
    @SerializedName("statsId")
    var id: String = "",
    var user: String = "",
    var userId: String = "",
    var physicalTotal: Int = 0,
    var digitalTotal: Int = 0,
    var completedGamesTotal: Int = 0,
    var lastGameCompleted: String = "",

    @set:PropertyName("platforms")
    @get:PropertyName("platforms")
    @SerializedName("platforms")
    var platformStats: List<PlatformStats> = listOf()
)