package com.jeeps.gamecollector.remaster.data.model.data.igdb

import com.google.gson.annotations.SerializedName

data class GameCoverIG(
    var id: Int = 0,
    var game: Int = 0,
    var height: Int = 0,
    var width: Int = 0,
    var url: String = "",
    @SerializedName("image_id")
    var imageId: String = ""

) {
    fun getBigCoverUrl(): String {
        return url
            .replace("//", "https://")
            .replace("t_thumb", "t_cover_big")
    }
}