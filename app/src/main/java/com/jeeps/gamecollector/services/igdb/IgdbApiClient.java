package com.jeeps.gamecollector.services.igdb;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class IgdbApiClient {
    private static final String BASE_URL = "https://api.igdb.com/";
    private static final String CLIENT_ID = "<client-id>";
    private static final String CLIENT_SECRET = "<client-secret>";
    private static String token = "";

    private static final String TWITCH_AUTH_URL =
            String.format("https://id.twitch.tv/oauth2/token?client_id=%s&client_secret=%s&grant_type=client_credentials",
                    CLIENT_ID, CLIENT_SECRET);

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

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
                                .header("Client-ID", CLIENT_ID)
                                .header("Authorization", token)
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

    public static void setToken() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(TWITCH_AUTH_URL)
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, ""))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    String body = responseBody.string();
                    Gson gson = new Gson();
                    TwitchAuthToken authToken = gson.fromJson(body, TwitchAuthToken.class);
                    token = String.format("Bearer %s", authToken.getAccess_token());
                }
            }
        });
    }
}
