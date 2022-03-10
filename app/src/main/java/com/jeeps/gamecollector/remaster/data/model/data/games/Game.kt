package com.jeeps.gamecollector.remaster.data.model.data.games

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by jeeps on 12/23/2017.
 */
data class Game(
    @SerializedName("gameId")
    var id: String = "",

    //Game data
    var user: String = "",
    var dateAdded: String = "",
    var imageUri: String = "",
    @field:JvmField
    var isPhysical: Boolean = true,
    var name: String = "",
    var shortName: String = "",
    var platformId: String = "",
    var platform: String = "",
    var publisherId: String = "",
    var publisher: String = "",
    var timesCompleted: Int = 0,
    var gameHoursStats: GameHoursStats = GameHoursStats(),

    @Expose(serialize = false, deserialize = false)
    var currentSortStat: String = ""
) : Serializable {
    constructor(
        imageUri: String, isPhysical: Boolean, name: String, shortName: String,
        platformId: String, platform: String, publisherId: String, publisher: String
    ) : this() {
        this.imageUri = imageUri
        this.isPhysical = isPhysical
        this.name = name
        this.shortName = shortName
        this.platformId = platformId
        this.platform = platform
        this.publisherId = publisherId
        this.publisher = publisher
    }
}