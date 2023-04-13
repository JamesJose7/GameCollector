package com.jeeps.gamecollector.deprecated.services.igdb;

import android.util.Log;

import com.jeeps.gamecollector.BuildConfig;
import com.jeeps.gamecollector.deprecated.services.api.ApiClient;
import com.jeeps.gamecollector.deprecated.services.api.UserService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class IgdbApiClient {
    private static final String BASE_URL = "https://api.igdb.com/";
    private static final String CLIENT_ID = BuildConfig.IGDB_CLIENT_ID;
    private static String token = "";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

    private static OkHttpClient.Builder httpClient;

    private static Retrofit retrofit;

    public static <S> S createService(
            Class<S> serviceClass) {
        httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("Client-ID", CLIENT_ID)
                                    .header("Authorization", token)
                                    .header("Content-Type", "text/plain")
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        });
        retrofit = builder
                .client(httpClient.build())
                .build();
        return retrofit.create(serviceClass);
    }

    public static void setToken() {
        UserService userService = ApiClient.createService(UserService.class);
        Call<TwitchAuthToken> call = userService.igdbAuth();
        call.enqueue(new Callback<TwitchAuthToken>() {
            @Override
            public void onResponse(Call<TwitchAuthToken> call, Response<TwitchAuthToken> response) {
                if (response.isSuccessful()) {
                    TwitchAuthToken authToken = response.body();
                    token = String.format("Bearer %s", authToken.getAccess_token());
                } else {
                    Log.e(IgdbApiClient.class.getSimpleName(), "There was a problem authenticating with Twitch");
                }
            }

            @Override
            public void onFailure(Call<TwitchAuthToken> call, Throwable t) {
                Log.e(IgdbApiClient.class.getSimpleName(), "Failed authenticating with Twitch");
            }
        });
    }
}
