package com.jeeps.gamecollector.remaster.data.model.data.games

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.jeeps.gamecollector.remaster.data.model.data.igdb.GameIG
import java.io.Serializable
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Created by jeeps on 12/23/2017.
 */

private const val RELEASE_DATE_FORMAT = "MMM dd, yyyy"

@kotlinx.serialization.Serializable
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
    var completionDate: String = "",
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
    var genresNames: List<String> = emptyList(),
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

    val completionDateParsed: LocalDate?
        get() = completionDate.ifEmpty { null }?.let {
            Instant.parse(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

    val completionDateFormatted: String
        get() {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return completionDateParsed?.format(formatter).orEmpty()
        }
}

fun Game.addAdditionalGameDetails(gameIG: GameIG, genresIg: List<String> = emptyList()) {
    firstReleaseDate = gameIG.firstReleaseDate ?: 0
    ageRatings = gameIG.ageRatings ?: emptyList()
    criticsRating = gameIG.criticsRating ?: 0.0
    criticsRatingCount = gameIG.criticsRatingCount ?: 0
    userRating = gameIG.userRating ?: 0.0
    userRatingCount = gameIG.userRatingCount ?: 0
    totalRating = gameIG.totalRating ?: 0.0
    totalRatingCount = gameIG.totalRatingCount ?: 0
    genres = gameIG.genres ?: emptyList()
    genresNames = genresIg
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

// Jetpack navigation decodes this slash that breaks the firestore link during serialization
fun Game?.encodeImageUriPath() : Game? = apply {
    if (this == null) return null
    if (imageUri.contains("/o/gameCovers/")) {
        imageUri = imageUri.replace("/o/gameCovers/", "/o/gameCovers%2F")
    }
}