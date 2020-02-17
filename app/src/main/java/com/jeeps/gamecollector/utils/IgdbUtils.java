package com.jeeps.gamecollector.utils;

public class IgdbUtils {
    private static String searchGamesQuery =
            "search \"%s\"; fields name,first_release_date, cover, category; where first_release_date != null; limit 15;";

    private static String coverImageQuery =
            "fields game,height,image_id,url,width; where id = %d;";

    public static String getSearchGamesQuery(String game) {
        return String.format(searchGamesQuery, game);
    }

    public static String getCoverImageQuery(int coverId) {
        return String.format(coverImageQuery, coverId);
    }
}
