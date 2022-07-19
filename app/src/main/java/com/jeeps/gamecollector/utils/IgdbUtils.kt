package com.jeeps.gamecollector.utils

object IgdbUtils {
    private const val searchGamesQuery =
        "search \"%s\"; fields name,first_release_date, cover, category, age_ratings, " +
                "aggregated_rating, aggregated_rating_count, rating, total_rating, total_rating_count, " +
                "genres, storyline, summary, url" +
                "; where first_release_date != null; limit 15;"
    private const val coverImageQuery = "fields game,height,image_id,url,width; where id = %d;"

    @JvmStatic
    fun getSearchGamesQuery(game: String?): String {
        return String.format(searchGamesQuery, game)
    }

    @JvmStatic
    fun getCoverImageQuery(coverId: Int): String {
        return String.format(coverImageQuery, coverId)
    }
}