package com.jeeps.gamecollector;

import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jeeps.gamecollector.model.Game;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;

import android.transition.Explode;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        platformId = intent.getStringExtra(PlatformLibraryActivity.CURRENT_PLATFORM);
        platformName = intent.getStringExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME);
        selectedGame = (Game) intent.getSerializableExtra(PlatformLibraryActivity.SELECTED_GAME);
        selectedGamePosition = intent.getIntExtra(PlatformLibraryActivity.SELECTED_GAME_POSITION, -1);

        populateViews();
    }

    private void populateViews() {
        Picasso.get().load(selectedGame.getImageUri()).into(gameCoverView);
        String title = !selectedGame.getShortName().isEmpty() ? selectedGame.getShortName() : selectedGame.getName();
        gameTitleText.setText(title);
        gamePublisherText.setText(selectedGame.getPublisher());
        gamePlatformText.setText(selectedGame.getPlatform());

        fabButton.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

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
}