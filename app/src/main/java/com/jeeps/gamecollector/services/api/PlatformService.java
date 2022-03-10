package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PlatformService {
    @GET("/api/platforms")
    Call<List<Platform>> getPlatformsByUser(@Header("Authorization") String authorization);

    @POST("/api/platforms")
    Call<Platform> postPlatform(@Header("Authorization") String authorization,
                                @Body Platform platform);

    @POST("/api/platforms/{platformId}")
    Call<Platform> editPlatform(@Header("Authorization") String authorization,
                                @Path("platformId") String platformId,
                                @Body Platform platform);

    @Multipart
    @POST("/api/platforms/{platformId}/image")
    Call<ResponseBody> uploadPlatformCover(@Header("Authorization") String authorization,
                                           @Path("platformId") String platformId,
                                           @Part MultipartBody.Part image);
}
