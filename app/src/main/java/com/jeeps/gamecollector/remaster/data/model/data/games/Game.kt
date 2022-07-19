package com.jeeps.gamecollector.remaster.data.model.data.games

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.jeeps.gamecollector.remaster.data.model.data.igdb.GameIG
import java.io.Serializable
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Created by jeeps on 12/23/2017.
 */

private const val RELEASE_DATE_FORMAT = "MMM dd, YYYY"

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

    // AdditionalDetails
    var firstReleaseDate: Long = 0,
    var ageRatings: List<Int> = emptyList(),
    var criticsRating: Double = 0.0,
    var criticsRatingCount: Int = 0,
    var userRating: Double = 0.0,
    var userRatingCount: Int = 0,
    var totalRating: Double = 0.0,
    var totalRatingCount: Int = 0,
    var genres: List<Int> = emptyList(),
    var storyline: String = "",
    var summary: String = "",
    var url: String = "",

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

fun Game.addAdditionalGameDetails(gameIG: GameIG) {
    firstReleaseDate = gameIG.firstReleaseDate
    ageRatings = gameIG.ageRatings ?: emptyList()
    criticsRating = gameIG.criticsRating ?: 0.0
    criticsRatingCount = gameIG.criticsRatingCount ?: 0
    userRating = gameIG.userRating ?: 0.0
    userRatingCount = gameIG.userRatingCount ?: 0
    totalRating = gameIG.totalRating ?: 0.0
    totalRatingCount = gameIG.totalRatingCount ?: 0
    genres = gameIG.genres ?: emptyList()
    storyline = gameIG.storyline ?: ""
    summary = gameIG.summary ?: ""
    url = gameIG.url ?: ""
}

fun Game?.releaseDateFormatted(): String {
    if (this == null || firstReleaseDate == 0L) return ""
    return try {
        val date = Instant.ofEpochSecond(firstReleaseDate)
        val formatter = DateTimeFormatter
            .ofPattern(RELEASE_DATE_FORMAT)
            .withZone(ZoneId.from(ZoneOffset.UTC))
        formatter.format(date)
    } catch (e: DateTimeException) {
        e.printStackTrace()
        ""
    }
}
