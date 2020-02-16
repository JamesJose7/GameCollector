package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.Platform;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface PlatformService {
    @GET("/api/platforms")
    Call<List<Platform>> getPlatformsByUser(@Header("Authorization") String authorization);
}
