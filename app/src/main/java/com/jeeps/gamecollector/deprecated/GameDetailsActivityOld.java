package com.jeeps.gamecollector.deprecated;

import static com.jeeps.gamecollector.deprecated.PlatformLibraryActivity.CURRENT_PLATFORM;
import static com.jeeps.gamecollector.deprecated.PlatformLibraryActivity.CURRENT_PLATFORM_NAME;
import static com.jeeps.gamecollector.deprecated.PlatformLibraryActivity.EDIT_GAME_RESULT;
import static com.jeeps.gamecollector.deprecated.PlatformLibraryActivity.NEW_GAME;
import static com.jeeps.gamecollector.deprecated.PlatformLibraryActivity.SELECTED_GAME;
import static com.jeeps.gamecollector.deprecated.PlatformLibraryActivity.SELECTED_GAME_POSITION;
import static com.jeeps.gamecollector.deprecated.utils.ColorsUtils.getColorByHoursRange;
import static com.jeeps.gamecollector.deprecated.utils.FormatUtils.formatDecimal;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.deprecated.model.CurrentUser;
import com.jeeps.gamecollector.deprecated.services.api.ApiClient;
import com.jeeps.gamecollector.deprecated.services.api.GameService;
import com.jeeps.gamecollector.deprecated.services.api.StatsService;
import com.jeeps.gamecollector.deprecated.utils.UserUtils;
import com.jeeps.gamecollector.remaster.data.model.data.games.Game;
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats;
import com.jeeps.gamecollector.remaster.data.model.data.games.ToggleCompletionResponse;
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetailsActivityOld extends AppCompatActivity {

    private static final String TAG = GameDetailsActivityOld.class.getSimpleName();

    ImageView gameCoverView;
    TextView gameTitleText;
    TextView gamePublisherText;
    TextView gamePlatformText;
    FloatingActionButton fabButton;
    SparkButton completeSwitch;

    TextView mainStoryHoursTv;
    TextView mainExtraHoursTv;
    TextView completionistHoursTv;

    private Toolbar toolbar;
    private String platformId;
    private String platformName;
    private Game selectedGame;
    private int selectedGamePosition;
    private CurrentUser currentUser;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup exit transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);
//        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        // Get Firestore instance
        db = FirebaseFirestore.getInstance();

        // Get local data
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);
        currentUser = UserUtils.getCurrentUser(this, sharedPreferences);

        //Get intent contents
        Intent intent = getIntent();
        platformId = intent.getStringExtra(CURRENT_PLATFORM);
        platformName = intent.getStringExtra(CURRENT_PLATFORM_NAME);
        selectedGame = (Game) intent.getSerializableExtra(SELECTED_GAME);
        selectedGamePosition = intent.getIntExtra(SELECTED_GAME_POSITION, -1);

        populateViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_GAME_RESULT) {
            if (data != null) {
                Game game = (Game) data.getSerializableExtra(NEW_GAME);
                int position = data.getIntExtra(SELECTED_GAME_POSITION, -1);

                Intent result = new Intent();
                result.putExtra(PlatformLibraryActivity.NEW_GAME, game);
                result.putExtra(PlatformLibraryActivity.SELECTED_GAME_POSITION, position);
                setResult(resultCode, result);
                finish();
            }
        }
    }

    private void populateViews() {
        if (!selectedGame.getImageUri().isEmpty())
            Picasso.get().load(selectedGame.getImageUri()).into(gameCoverView);
        String title = !selectedGame.getShortName().isEmpty() ? selectedGame.getShortName() : selectedGame.getName();
        gameTitleText.setText(title);
        if (selectedGame.getPublisher().isEmpty())
            gamePublisherText.setVisibility(View.GONE);
        else
            gamePublisherText.setText(selectedGame.getPublisher());
        gamePlatformText.setText(selectedGame.getPlatform());

        // Game completion button
        completeSwitch.setChecked(selectedGame.getTimesCompleted() > 0);
        setupCompleteSwitch();

        fabButton.setOnClickListener(view -> editGame());

        getCoverColors();

        if (selectedGame.getGameHoursStats() != null) {
            formatGameplayHours(new GameplayHoursStats(selectedGame.getGameHoursStats()));
        }

        getGameplayHours(selectedGame.getName());
    }

    private void setupCompleteSwitch() {
        completeSwitch.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                updateGameCompletion(buttonState);
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) { }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {}
        });
    }

    private void updateGameCompletion(boolean completed) {
        GameService gameService = ApiClient.createService(GameService.class);
        Call<ToggleCompletionResponse> toggleGameCompletion = gameService.toggleGameCompletion("Bearer " + currentUser.getToken(),
                selectedGame.getId());
        toggleGameCompletion.enqueue(new Callback<ToggleCompletionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ToggleCompletionResponse> call, @NonNull Response<ToggleCompletionResponse> response) {
                if (response.isSuccessful()) {
                    ToggleCompletionResponse completionResponse = response.body();
                    if (completionResponse != null) {
                        String message = completionResponse.isCompleted() ?
                                "Marked as complete" :
                                "Marked as incomplete";
                        Snackbar.make(fabButton, message, Snackbar.LENGTH_SHORT).show();
                        selectedGame.setTimesCompleted(completionResponse.isCompleted() ? 1 : 0);
                    }
                } else {
                    Log.e(TAG, "Toggle game completion request failed");
                    Toast.makeText(GameDetailsActivityOld.this, "There was an error when updating the game, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ToggleCompletionResponse> call, Throwable t) {
                Log.e(TAG, "Toggle game completion request failed");
                Toast.makeText(GameDetailsActivityOld.this, "There was an error when updating the game, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCoverColors() {
        new Thread(() -> {
            try {
                URL url = new URL(selectedGame.getImageUri());
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Palette.generateAsync(image, palette -> {
                    int mainColor = palette.getDominantColor(getResources().getColor(R.color.colorPrimary));
                    animateStatusBarColor(mainColor);
                });
            } catch (IOException e) {
                System.out.println(e);
            }
        }).start();

    }

    private void animateStatusBarColor(int colorTo) {
        int colorFrom = getResources().getColor(R.color.colorPrimary);
        ObjectAnimator colorAnimation = ObjectAnimator.ofObject(toolbar, "backgroundColor",
                new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300);
        colorAnimation.start();

        getWindow().setStatusBarColor(colorTo);
    }

    private void editGame() {
        //Start add game activity to edit selected
        Intent intent = new Intent(this, AddGameActivityOld.class);
        intent.putExtra(CURRENT_PLATFORM, platformId);
        intent.putExtra(CURRENT_PLATFORM_NAME, platformName);
        intent.putExtra(SELECTED_GAME, selectedGame);
        intent.putExtra(SELECTED_GAME_POSITION, selectedGamePosition);

        startActivityForResult(intent, EDIT_GAME_RESULT);
    }

    private void getGameplayHours(String gameName) {
        StatsService statsService = ApiClient.createService(StatsService.class);
        statsService.getGameHours(gameName)
                .enqueue(new Callback<GameplayHoursStats>() {
                    @Override
                    public void onResponse(@NotNull Call<GameplayHoursStats> call,
                                           @NotNull Response<GameplayHoursStats> response) {
                        GameplayHoursStats stats = response.body();
                        if (stats != null) {
                            if (selectedGame.getGameHoursStats() == null ||
                                isStoredHoursDifferentFromIgbd(selectedGame.getGameHoursStats(), stats)) {
                                formatGameplayHours(stats);
                                updateGameHours(stats);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<GameplayHoursStats> call,
                                          @NotNull Throwable t) {
                        t.printStackTrace();
                        // Hide gameplay hours card
                    }
                });
    }

    private void updateGameHours(GameplayHoursStats stats) {
        db.collection("games")
                .document(selectedGame.getId())
                .update("gameHoursStats", new GameHoursStats(stats))
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed when updating gameplay hours", Toast.LENGTH_LONG).show();
                });
    }

    private void formatGameplayHours(GameplayHoursStats stats) {
        mainStoryHoursTv.setText(getString(R.string.hours_template, formatDecimal(stats.getGameplayMain())));
        mainStoryHoursTv.setTextColor(getColorByHoursRange(
                GameDetailsActivityOld.this, stats.getGameplayMain()));

        mainExtraHoursTv.setText(getString(R.string.hours_template, formatDecimal(stats.getGameplayMainExtra())));
        mainExtraHoursTv.setTextColor(getColorByHoursRange(
                GameDetailsActivityOld.this, stats.getGameplayMainExtra()));

        completionistHoursTv.setText(getString(R.string.hours_template, formatDecimal(stats.getGameplayCompletionist())));
        completionistHoursTv.setTextColor(getColorByHoursRange(
                GameDetailsActivityOld.this, stats.getGameplayCompletionist()));
    }

    private boolean isStoredHoursDifferentFromIgbd(GameHoursStats storedHours, GameplayHoursStats igdbHours) {
        return storedHours.getGameplayCompletionist() != igdbHours.getGameplayCompletionist() ||
                storedHours.getGameplayMain() != igdbHours.getGameplayMain() ||
                storedHours.getGameplayMainExtra() != igdbHours.getGameplayMainExtra();
    }
}