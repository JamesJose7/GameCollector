package com.jeeps.gamecollector.remaster.utils

object IgdbUtils {
    private const val searchGamesQuery =
        "search \"%s\"; fields name,first_release_date, cover, category, age_ratings, " +
                "aggregated_rating, aggregated_rating_count, rating, rating_count, total_rating, total_rating_count, " +
                "genres, storyline, summary, url" +
                "; where first_release_date != null; limit 15;"
    private const val coverImageQuery = "fields game,height,image_id,url,width; where id = %d;"
    private const val genresQuery = "fields checksum,created_at,name,slug,updated_at,url; where id = (%s);"

    @JvmStatic
    fun getSearchGamesQuery(game: String?): String {
        return String.format(searchGamesQuery, game)
    }

    @JvmStatic
    fun getCoverImageQuery(coverId: Int): String {
        return String.format(coverImageQuery, coverId)
    }

    @JvmStatic
    fun getGameGenresQuery(genreIds: List<Int>): String {
        val idsQuery = genreIds.joinToString(",")
        return String.format(genresQuery, idsQuery)
    }
}