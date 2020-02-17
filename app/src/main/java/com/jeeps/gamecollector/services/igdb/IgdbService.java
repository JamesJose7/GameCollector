package com.jeeps.gamecollector.services.igdb;

import com.jeeps.gamecollector.model.igdb.GameCoverIG;
import com.jeeps.gamecollector.model.igdb.GameIG;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IgdbService {

    @POST("/games")
    Call<List<GameIG>> searchGames(@Body String body);

    @POST("/covers")
    Call<List<GameCoverIG>> getImageCoverById(@Body String body);
}
