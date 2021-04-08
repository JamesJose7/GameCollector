package com.jeeps.gamecollector.services.igdb;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class IgdbApiClient {
    private static final String BASE_URL = "https://api-v3.igdb.com/";
    private static final String API_KEY = "6c2e3bc7e808d77d2d0023f5ea715fff";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("user-key", API_KEY)
                                .header("Content-Type", "text/plain")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    });

    private static Retrofit retrofit = builder
            .client(httpClient.build())
            .build();

    public static <S> S createService(
            Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
