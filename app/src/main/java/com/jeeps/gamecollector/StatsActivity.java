package com.jeeps.gamecollector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.Platform;
import com.jeeps.gamecollector.model.Publisher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StatsActivity extends AppCompatActivity {

    private static final String LIBRARY_URL = "https://gamecollector-59155.firebaseio.com/library.json";

    private final OkHttpClient client = new OkHttpClient();

    private List<Integer> gameCountPerPlatform = new ArrayList<>();

    //Views
    @BindView(R.id.total_games)
    TextView totalGamesText;
    @BindView(R.id.total_publishers)
    TextView totalPublishersText;

    private List<Game> mGames;
    private List<Platform> mPlatforms;
    private List<Publisher> mPublishers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        //Change title
        getSupportActionBar().setTitle("Statistics");


        try {
            getJson();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getJson() throws Exception {
        Request request = new Request.Builder()
                .url(LIBRARY_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    //Parse json
                    parseJson(responseBody.string());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseJson(String string) throws JSONException {
        JSONObject jsonObject = new JSONObject(string);

        //Get json arrays
        final JSONArray gamesJSON = jsonObject.getJSONArray("games");
        JSONArray platformsJSON = jsonObject.getJSONArray("platforms");
        JSONArray publishersJSON = jsonObject.getJSONArray("publishers");

        //Parse json into objects
        mGames = getGames(gamesJSON);
        mPlatforms = getPlatforms(platformsJSON);
        mPublishers = getPublishers(publishersJSON);

        //Display data on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayData();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayData() {
        //Overall stats
        totalGamesText.setText(mGames.size() + "");
        totalPublishersText.setText(mPublishers.size() + "");
    }

    private List<Game> getGames(JSONArray games) throws JSONException {
        List<Game> gamesList = new ArrayList<>();
        List<JSONArray> gamesKeysPerPlatform = new ArrayList<>();
        //Game keys names and count per platform
        for (int i = 0; i < games.length(); i++) {
            JSONArray keys = games.getJSONObject(i).names();
            gamesKeysPerPlatform.add(keys);
            gameCountPerPlatform.add(keys.length());

            //Get all games
            JSONArray gameKeys = gamesKeysPerPlatform.get(i);
            for (int j = 0; j < gameKeys.length(); j++) {
                String key = gameKeys.getString(j);

                //Map game
                JSONObject platformGames = games.getJSONObject(i);
                JSONObject gameJson = platformGames.getJSONObject(key);
                //Create game and parse it from json
                Game game = new Game();
                game.jsonToGame(gameJson);
                gamesList.add(game);
            }
        }

        return gamesList;
    }


    private List<Publisher> getPublishers(JSONArray publishers) throws JSONException {
        List<Publisher> publishersList = new ArrayList<>();
        for (int i = 0; i < publishers.length(); i++) {
            Publisher publisher = new Publisher();
            publisher.jsonToPublisher(publishers.getJSONObject(i));
            publishersList.add(publisher);
        }
        return publishersList;
    }

    private List<Platform> getPlatforms(JSONArray platforms) throws JSONException {
        List<Platform> platformsList = new ArrayList<>();
        for (int i = 0; i < platforms.length(); i++) {
            Platform platform = new Platform();
            platform.jsonToPlatform(platforms.getJSONObject(i));
            platformsList.add(platform);
        }
        return platformsList;
    }
}
