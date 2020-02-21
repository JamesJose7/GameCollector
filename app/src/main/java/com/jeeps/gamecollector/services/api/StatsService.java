package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.UserStats;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface StatsService {
    @GET("/api/stats")
    Call<UserStats> getUserStats(@Header("Authorization") String authorization);
}
