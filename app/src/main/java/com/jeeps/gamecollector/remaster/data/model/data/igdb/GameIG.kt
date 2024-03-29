package com.jeeps.gamecollector.remaster.data.model.data.igdb

import com.google.gson.annotations.SerializedName
import com.jeeps.gamecollector.remaster.utils.extensions.similarity

data class GameIG(
    var id: Int = 0,
    var category: Int = 0,
    var cover: Int = 0,
    @SerializedName("first_release_date")
    var firstReleaseDate: Long? = 0,
    var name: String = "",
    @SerializedName("age_ratings")
    var ageRatings: List<Int>?,
    @SerializedName("aggregated_rating")
    var criticsRating: Double?,
    @SerializedName("aggregated_rating_count")
    var criticsRatingCount: Int?,
    @SerializedName("rating")
    var userRating: Double?,
    @SerializedName("rating_count")
    var userRatingCount: Int?,
    @SerializedName("total_rating")
    var totalRating: Double?,
    @SerializedName("total_rating_count")
    var totalRatingCount: Int?,

    var genres: List<Int>?,
    var storyline: String?,
    var summary: String?,
    var url: String?
)

fun List<GameIG>?.findMostSimilarGame(gameName: String): GameIG? {
    return if (this == null || isEmpty()) {
        null
    } else {
        // Exclude DLC and sort based on most similar name based on the user input
        val sortedGames = sortedByDescending { igGame -> igGame.name.similarity(gameName) }

        sortedGames
            .firstOrNull { igGame -> igGame.category != 1 }
    }
}