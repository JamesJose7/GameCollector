package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.UserStats;
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface StatsService {
    @GET("/api/stats")
    Call<UserStats> getUserStats(@Header("Authorization") String authorization);

    @GET("/api/getGameHours")
    Call<GameplayHoursStats> getGameHours(@Query("name") String gameName);
}
