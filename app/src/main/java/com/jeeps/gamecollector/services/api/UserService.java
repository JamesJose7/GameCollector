package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.UserDetails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserService {
    @GET("/api/user")
    Call<UserDetails> getUser(@Header("Authorization") String authorization);
}
