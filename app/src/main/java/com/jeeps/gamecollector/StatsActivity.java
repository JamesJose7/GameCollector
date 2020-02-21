package com.jeeps.gamecollector;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.db.williamchart.view.DonutChartView;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.jeeps.gamecollector.model.CurrentUser;
import com.jeeps.gamecollector.model.UserStats;
import com.jeeps.gamecollector.services.api.ApiClient;
import com.jeeps.gamecollector.services.api.StatsService;
import com.jeeps.gamecollector.utils.UserUtils;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

public class StatsActivity extends AppCompatActivity {

    private static final String LIBRARY_URL = "https://gamecollector-59155.firebaseio.com/library.json";
    private static final String TAG = StatsActivity.class.getSimpleName();

    @BindView(R.id.overall_completion_chart) DonutChartView overallCompletionChart;
    @BindView(R.id.overall_completion_percentage) TextView overallCompletionPercentage;
    @BindView(R.id.overall_total) TextView overallTotal;
    @BindView(R.id.overall_completed) TextView overallCompleted;
    @BindView(R.id.overall_total_physical) TextView overallTotalPhysical;
    @BindView(R.id.overall_total_digital) TextView overallTotalDigital;
    @BindView(R.id.overall_last_game_completed) TextView overallLastGameCompleted;

    @BindView(R.id.card_stats_container) RelativeLayout cardStatsContainer;
    @BindView(R.id.overall_card) CardView overallCard;
    @BindView(R.id.platforms_card) CardView platformsCard;
    @BindView(R.id.stats_progress_bar) ProgressBar statsProgressBar;

    private CurrentUser currentUser;
    private Context context;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        //Change title
        getSupportActionBar().setTitle("Statistics");

        context = this;
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);
        // Load current user
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);

        //Progress bar animation
        DoubleBounce doubleBounce = new DoubleBounce();
        statsProgressBar.setIndeterminateDrawable(doubleBounce);

        //Hide card stats
        cardStatsContainer.setVisibility(View.INVISIBLE);
        //Show progress bar
        statsProgressBar.setVisibility(View.VISIBLE);

        // Get stats
        getUserStats();
    }

    private void getUserStats() {
        StatsService statsService = ApiClient.createService(StatsService.class);
        Call<UserStats> getUserStats = statsService.getUserStats("Bearer " + currentUser.getToken());
        getUserStats.enqueue(new retrofit2.Callback<UserStats>() {
            @Override
            public void onResponse(Call<UserStats> call, retrofit2.Response<UserStats> response) {
                if (response.isSuccessful())
                    populateDashboard(response.body());
                else {
                    Log.e(TAG, "There was an error retrieving the user stats");
                    Toast.makeText(context, "There was an error retrieving your stats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserStats> call, Throwable t) {
                Log.e(TAG, "There was an error requesting user stats");
                Toast.makeText(context, "There was an error retrieving your stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDashboard(UserStats userStats) {
        // Calculate total games and completion percentage
        int totalGames = userStats.getPhysicalTotal() + userStats.getDigitalTotal();
        float completionPercentage = Math.round((userStats.getCompletedGamesTotal() * 100) / totalGames);
        System.out.println(completionPercentage);
        // Populate views
        overallTotal.setText(String.valueOf(totalGames));
        overallCompleted.setText(String.valueOf(userStats.getCompletedGamesTotal()));
        overallTotalPhysical.setText(String.valueOf(userStats.getPhysicalTotal()));
        overallTotalDigital.setText(String.valueOf(userStats.getDigitalTotal()));
        overallCompletionPercentage.setText(String.format("%d%%", (int) completionPercentage));
        overallLastGameCompleted.setText(userStats.getLastGameCompleted());
        // Chart
        int[] colors = {Color.parseColor("#FF5722")};
        overallCompletionChart.setDonutColors(colors);
        overallCompletionChart.getAnimation().setDuration(1000L);
        overallCompletionChart.animate(Collections.singletonList(completionPercentage));

        //Hide Progress bar and show cards
        statsProgressBar.setVisibility(View.INVISIBLE);
        //cardStatsContainer.setVisibility(View.VISIBLE);
        animateAppearance();
    }

    private void animateAppearance() {
        //Get coordinates values
        int firstCardStartValue = overallCard.getTop();
        int firstCardEndValue = overallCard.getBottom();
        int おはよう = platformsCard.getTop();
        int secondCardEndValue = platformsCard.getBottom();

        //Slide animation on cards
        ObjectAnimator firstCardAnimator = ObjectAnimator.ofInt(overallCard, "bottom", firstCardStartValue, firstCardEndValue);
        ObjectAnimator secondCardAnimator = ObjectAnimator.ofInt(platformsCard, "bottom", おはよう, secondCardEndValue);

        //Modify animators
        firstCardAnimator.setInterpolator(new AccelerateInterpolator());
        secondCardAnimator.setInterpolator(new AccelerateInterpolator());
        firstCardAnimator.setDuration(500);
        secondCardAnimator.setDuration(500);

        //Hide them before showing them
        overallCard.setBottom(firstCardStartValue);
        platformsCard.setBottom(おはよう);

        cardStatsContainer.setVisibility(View.VISIBLE);

        //Sequential animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(firstCardAnimator, secondCardAnimator);

        //Start
        animatorSet.start();
    }
}
