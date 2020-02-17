package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.Game;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface GameService {
    @GET("/api/games/{username}/{platformId}")
    Call<List<Game>> getGamesByUserAndPlatform(@Header("Authorization") String authorization,
                                               @Path("username") String username,
                                               @Path("platformId") String platformId);
}
