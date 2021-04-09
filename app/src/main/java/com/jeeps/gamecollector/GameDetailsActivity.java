package com.jeeps.gamecollector;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jeeps.gamecollector.model.Game;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jeeps.gamecollector.PlatformLibraryActivity.CURRENT_PLATFORM;
import static com.jeeps.gamecollector.PlatformLibraryActivity.CURRENT_PLATFORM_NAME;
import static com.jeeps.gamecollector.PlatformLibraryActivity.EDIT_GAME_RESULT;
import static com.jeeps.gamecollector.PlatformLibraryActivity.NEW_GAME;
import static com.jeeps.gamecollector.PlatformLibraryActivity.SELECTED_GAME;
import static com.jeeps.gamecollector.PlatformLibraryActivity.SELECTED_GAME_POSITION;

public class GameDetailsActivity extends AppCompatActivity {

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

    private String platformId;
    private String platformName;
    private Game selectedGame;
    private int selectedGamePosition;
    private Toolbar toolbar;

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

        fabButton.setOnClickListener(view -> editGame());

        getCoverColors();
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
//        int colorTo = Color.parseColor("#00ff00");
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