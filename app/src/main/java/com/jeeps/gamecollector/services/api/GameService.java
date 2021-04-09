package com.jeeps.gamecollector.services.api;

import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.ToggleCompletionResponse;

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

public interface GameService {
    @GET("/api/games/{username}/{platformId}")
    Call<List<Game>> getGamesByUserAndPlatform(@Header("Authorization") String authorization,
                                               @Path("username") String username,
                                               @Path("platformId") String platformId);

    @POST("/api/games")
    Call<Game> postGame(@Header("Authorization") String authorization,
                        @Body Game game);

    @POST("/api/games/{gameId}")
    Call<ResponseBody> editGame(@Header("Authorization") String authorization,
                        @Path("gameId") String gameId,
                        @Body Game game);

    @POST("/api/games/{gameId}/delete")
    Call<ResponseBody> deleteGame(@Header("Authorization") String authorization,
                                  @Path("gameId") String gameId);

    @Multipart
    @POST("/api/games/{gameId}/image")
    Call<ResponseBody> uploadGameCover(@Header("Authorization") String authorization,
                                       @Path("gameId") String gameId,
                                       @Part MultipartBody.Part body);

    @POST("/api/games/toggleCompletion/{gameId}")
    Call<ToggleCompletionResponse> toggleGameCompletion(@Header("Authorization") String authorization,
                                                        @Path("gameId") String gameId);
}
