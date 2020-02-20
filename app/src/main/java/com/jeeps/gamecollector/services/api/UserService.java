package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.User;
import com.jeeps.gamecollector.model.UserDetails;

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
}
