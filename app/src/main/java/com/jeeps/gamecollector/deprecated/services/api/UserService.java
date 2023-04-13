package com.jeeps.gamecollector.deprecated.services.api;

import com.jeeps.gamecollector.remaster.data.model.data.user.User;
import com.jeeps.gamecollector.remaster.data.model.data.user.UserDetails;
import com.jeeps.gamecollector.deprecated.services.igdb.TwitchAuthToken;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserService {
    @GET("/api/user")
    Call<UserDetails> getUser(@Header("Authorization") String authorization);

    @POST("/api/signupUserdetails")
    Call<ResponseBody> signupUserdetails(@Body User user);

    @GET("/api/igdbAuth")
    Call<TwitchAuthToken> igdbAuth();
}
