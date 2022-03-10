package com.jeeps.gamecollector.remaster.data.model.data.igdb

import com.google.gson.annotations.SerializedName

data class GameIG(
    var id: Int = 0,
    var category: Int = 0,
    var cover: Int = 0,
    @SerializedName("first_release_date")
    var firstReleaseDate: Long = 0,
    var name: String = ""
)