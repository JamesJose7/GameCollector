package com.jeeps.gamecollector.deprecated;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.db.williamchart.view.DonutChartView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.remaster.ui.adapters.PlatformStatsAdapter;
import com.jeeps.gamecollector.deprecated.model.CurrentUser;
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats;
import com.jeeps.gamecollector.remaster.data.model.data.user.UserStats;
import com.jeeps.gamecollector.deprecated.utils.UserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatsActivity extends AppCompatActivity {

    private static final String LIBRARY_URL = "https://gamecollector-59155.firebaseio.com/library.json";
    private static final String TAG = StatsActivity.class.getSimpleName();

    DonutChartView overallCompletionChart;
    TextView overallCompletionPercentage;
    TextView overallTotal;
    TextView overallCompleted;
    TextView overallTotalPhysical;
    TextView overallTotalDigital;
    TextView overallLastGameCompleted;

    RelativeLayout cardStatsContainer;
    CardView overallCard;
    CardView platformsCard;
    ProgressBar statsProgressBar;

    RecyclerView platformsRecyclerView;

    private CurrentUser currentUser;
    private Context context;
    private SharedPreferences sharedPreferences;
    private RecyclerView.LayoutManager layoutManager;
    private PlatformStatsAdapter platformStatsAdapter;
    private List<PlatformStats> platformsStats;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ButterKnife.bind(this);

        //Change title
        getSupportActionBar().setTitle("Statistics");

        // Get Firestore instance
        db = FirebaseFirestore.getInstance();

        context = this;
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);
        // Load current user
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);

        //Progress bar animation
//        DoubleBounce doubleBounce = new DoubleBounce();
//        statsProgressBar.setIndeterminateDrawable(doubleBounce);

        // Configure recycler view
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        platformsRecyclerView.setHasFixedSize(true);
        platformsRecyclerView.setLayoutManager(layoutManager);
        platformsStats = new ArrayList<>();
        platformStatsAdapter = new PlatformStatsAdapter(platformsStats);
        platformsRecyclerView.setAdapter(platformStatsAdapter);

        // Overall Chart
        int[] colors = {Color.parseColor("#FF5722")};
        overallCompletionChart.setDonutColors(colors);
        overallCompletionChart.getAnimation().setDuration(1000L);
        overallCompletionChart.animate(Collections.singletonList(0f));

        //Hide card stats
//        cardStatsContainer.setVisibility(View.INVISIBLE);
        //Show progress bar
        statsProgressBar.setVisibility(View.VISIBLE);

        // Get stats
        getUserStats();
    }

    private void getUserStats() {
        db.collection("stats")
                .whereEqualTo("user", currentUser.getUsername())
                .limit(1)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Log.e(TAG, "There was an error requesting user stats");
                        Toast.makeText(context, "There was an error retrieving your stats", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserStats userStats =
                            queryDocumentSnapshots.getDocuments().get(0).toObject(UserStats.class);
                    populateDashboard(userStats);
                });
    }

    private void populateDashboard(UserStats userStats) {
        if (userStats.getPlatformStats().isEmpty()) {
            Snackbar.make(cardStatsContainer, "You don't have any stats yet, Start by creating a platform", Snackbar.LENGTH_SHORT).show();
            statsProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        // Calculate total games and completion percentage
        int totalGames = userStats.getPhysicalTotal() + userStats.getDigitalTotal();
        float completionPercentage = 0;
        if (totalGames > 0)
            completionPercentage = Math.round((userStats.getCompletedGamesTotal() * 100) / totalGames);
        // Populate views
        overallTotal.setText(String.valueOf(totalGames));
        overallCompleted.setText(String.valueOf(userStats.getCompletedGamesTotal()));
        overallTotalPhysical.setText(String.valueOf(userStats.getPhysicalTotal()));
        overallTotalDigital.setText(String.valueOf(userStats.getDigitalTotal()));
        overallCompletionPercentage.setText(String.format("%d%%", (int) completionPercentage));
        overallLastGameCompleted.setText(userStats.getLastGameCompleted());
        // Chart
        overallCompletionChart.animate(Collections.singletonList(completionPercentage));
        // PLatform stats
        platformsStats.clear();
        platformsStats.addAll(userStats.getPlatformStats());
        platformStatsAdapter.notifyDataSetChanged();

        //Hide Progress bar and show cards
        statsProgressBar.setVisibility(View.INVISIBLE);
        //cardStatsContainer.setVisibility(View.VISIBLE);
//        animateAppearance();
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
