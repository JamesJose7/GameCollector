package com.jeeps.gamecollector.services.igdb;

import com.jeeps.gamecollector.remaster.data.model.data.igdb.GameCoverIG;
import com.jeeps.gamecollector.remaster.data.model.data.igdb.GameIG;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IgdbService {

    @POST("/v4/games")
    Call<List<GameIG>> searchGames(@Body String body);

    @POST("/v4/covers")
    Call<List<GameCoverIG>> getImageCoverById(@Body String body);
}
