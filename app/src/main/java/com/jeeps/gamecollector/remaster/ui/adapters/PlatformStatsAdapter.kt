package com.jeeps.gamecollector.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.db.williamchart.view.DonutChartView;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats;

import java.util.Collections;
import java.util.List;

public class PlatformStatsAdapter extends RecyclerView.Adapter<PlatformStatsAdapter.PlatformViewHolder> {
    private Context context;
    private List<PlatformStats> platformsStats;

    @NonNull
    @Override
    public PlatformViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.platform_stats_item_layout, parent, false);
        return new PlatformViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatformViewHolder holder, int position) {
        PlatformStats platformStats = platformsStats.get(position);
        // Calculate total games and completion percentages
        int totalGames = platformStats.getDigitalTotal() + platformStats.getPhysicalTotal();
        float completionPercentage = 0;
        if (totalGames > 0)
            completionPercentage = Math.round((platformStats.getCompletedGamesTotal() * 100) / totalGames);
        // Populate views
        holder.platformName.setText(platformStats.getPlatformName());
        holder.platformTotal.setText(String.valueOf(totalGames));
        holder.platformCompleted.setText(String.valueOf(platformStats.getCompletedGamesTotal()));
        holder.platformTotalPhysical.setText(String.valueOf(platformStats.getPhysicalTotal()));
        holder.platformTotalDigital.setText(String.valueOf(platformStats.getDigitalTotal()));
        holder.platformCompletionPercentage.setText(String.format("%d%%", (int) completionPercentage));
        holder.platformLastGameCompleted.setText(platformStats.getLastGameCompleted());
        // Chart
        int[] colors = {Color.parseColor("#FF5722")};
        holder.platformChart.setDonutColors(colors);
        holder.platformChart.getAnimation().setDuration(1000L);
        holder.platformChart.animate(Collections.singletonList(completionPercentage));
    }

    @Override
    public int getItemCount() {
        return platformsStats.size();
    }

    protected class PlatformViewHolder extends RecyclerView.ViewHolder {
        TextView platformName;
        DonutChartView platformChart;
        TextView platformCompletionPercentage;
        TextView platformTotal;
        TextView platformCompleted;
        TextView platformTotalPhysical;
        TextView platformTotalDigital;
        TextView platformLastGameCompleted;

        public PlatformViewHolder(@NonNull View itemView) {
            super(itemView);
            platformName = itemView.findViewById(R.id.platform_name);
            platformChart = itemView.findViewById(R.id.platform_completion_chart);
            platformCompletionPercentage = itemView.findViewById(R.id.platform_completion_percentage);
            platformTotal = itemView.findViewById(R.id.platform_total);
            platformCompleted = itemView.findViewById(R.id.platform_completed);
            platformTotalPhysical = itemView.findViewById(R.id.platform_total_physical);
            platformTotalDigital = itemView.findViewById(R.id.platform_total_digital);
            platformLastGameCompleted = itemView.findViewById(R.id.platform_last_game_completed);
        }
    }

    public PlatformStatsAdapter(Context context, List<PlatformStats> platformsStats) {
        this.context = context;
        this.platformsStats = platformsStats;
    }
}
