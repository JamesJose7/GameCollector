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
    val id: String = "",

    //Game data
    val user: String = "",
    val dateAdded: String = "",
    val imageUri: String = "",
    @field:JvmField
    val isPhysical: Boolean = true,
    val name: String = "",
    val shortName: String = "",
    val platformId: String = "",
    val platform: String = "",
    val publisherId: String = "",
    val publisher: String = "",
    val timesCompleted: Int = 0,
    val completionDate: String = "",
    val gameHoursStats: GameHoursStats = GameHoursStats(),

    // AdditionalDetails
    val firstReleaseDate: Long = 0,
    val ageRatings: List<Int> = emptyList(),
    val criticsRating: Double = 0.0,
    val criticsRatingCount: Int = 0,
    val userRating: Double = 0.0,
    val userRatingCount: Int = 0,
    val totalRating: Double = 0.0,
    val totalRatingCount: Int = 0,
    val genres: List<Int> = emptyList(),
    val genresNames: List<String> = emptyList(),
    val storyline: String = "",
    val summary: String = "",
    val url: String = "",

    @Expose(serialize = false, deserialize = false)
    val currentSortStat: String = ""
) : Serializable {

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

fun Game.addAdditionalGameDetails(gameIG: GameIG, genresIg: List<String> = emptyList()): Game {
    return copy(
        firstReleaseDate = gameIG.firstReleaseDate ?: 0,
        ageRatings = gameIG.ageRatings ?: emptyList(),
        criticsRating = gameIG.criticsRating ?: 0.0,
        criticsRatingCount = gameIG.criticsRatingCount ?: 0,
        userRating = gameIG.userRating ?: 0.0,
        userRatingCount = gameIG.userRatingCount ?: 0,
        totalRating = gameIG.totalRating ?: 0.0,
        totalRatingCount = gameIG.totalRatingCount ?: 0,
        genres = gameIG.genres ?: emptyList(),
        genresNames = genresIg,
        storyline = gameIG.storyline ?: "",
        summary = gameIG.summary ?: "",
        url = gameIG.url ?: ""
    )
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
fun Game?.encodeImageUriPath() : Game? {
    if (this == null) return null
    return if (imageUri.contains("/o/gameCovers/")) {
        copy(
            imageUri = imageUri.replace("/o/gameCovers/", "/o/gameCovers%2F")
        )
    } else this
}