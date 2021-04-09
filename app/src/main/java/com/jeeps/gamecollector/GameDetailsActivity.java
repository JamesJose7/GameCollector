package com.jeeps.gamecollector;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jeeps.gamecollector.model.CurrentUser;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.ToggleCompletionResponse;
import com.jeeps.gamecollector.services.api.ApiClient;
import com.jeeps.gamecollector.services.api.GameService;
import com.jeeps.gamecollector.utils.UserUtils;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.jeeps.gamecollector.PlatformLibraryActivity.CURRENT_PLATFORM;
import static com.jeeps.gamecollector.PlatformLibraryActivity.CURRENT_PLATFORM_NAME;
import static com.jeeps.gamecollector.PlatformLibraryActivity.EDIT_GAME_RESULT;
import static com.jeeps.gamecollector.PlatformLibraryActivity.NEW_GAME;
import static com.jeeps.gamecollector.PlatformLibraryActivity.SELECTED_GAME;
import static com.jeeps.gamecollector.PlatformLibraryActivity.SELECTED_GAME_POSITION;

public class GameDetailsActivity extends AppCompatActivity {

    private static final String TAG = GameDetailsActivity.class.getSimpleName();

    @BindView(R.id.game_cover)
    ImageView gameCoverView;
    @BindView(R.id.game_title)
    TextView gameTitleText;
    @BindView(R.id.game_publisher)
    TextView gamePublisherText;
    @BindView(R.id.game_platform)
    TextView gamePlatformText;
    @BindView(R.id.fab)
    FloatingActionButton fabButton;
    @BindView(R.id.complete_switch)
    SparkButton completeSwitch;

    private Toolbar toolbar;
    private String platformId;
    private String platformName;
    private Game selectedGame;
    private int selectedGamePosition;
    private CurrentUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup exit transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

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
            Game game = (Game) data.getSerializableExtra(NEW_GAME);
            int position = data.getIntExtra(SELECTED_GAME_POSITION, -1);

            Intent result = new Intent();
            result.putExtra(PlatformLibraryActivity.NEW_GAME, game);
            result.putExtra(PlatformLibraryActivity.SELECTED_GAME_POSITION, position);
            setResult(resultCode, result);
            finish();
        }
    }

    private void populateViews() {
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
            public void onResponse(Call<ToggleCompletionResponse> call, Response<ToggleCompletionResponse> response) {
                if (response.isSuccessful()) {
                    ToggleCompletionResponse completionResponse = response.body();
                    String message = completionResponse.isCompleted() ?
                            "Marked as complete" :
                            "Marked as incomplete";
                    Snackbar.make(fabButton, message, Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Toggle game completion request failed");
                    Toast.makeText(GameDetailsActivity.this, "There was an error when updating the game, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ToggleCompletionResponse> call, Throwable t) {
                Log.e(TAG, "Toggle game completion request failed");
                Toast.makeText(GameDetailsActivity.this, "There was an error when updating the game, please try again", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, AddGameActivity.class);
        intent.putExtra(CURRENT_PLATFORM, platformId);
        intent.putExtra(CURRENT_PLATFORM_NAME, platformName);
        intent.putExtra(SELECTED_GAME, selectedGame);
        intent.putExtra(SELECTED_GAME_POSITION, selectedGamePosition);

        startActivityForResult(intent, EDIT_GAME_RESULT);
    }
}