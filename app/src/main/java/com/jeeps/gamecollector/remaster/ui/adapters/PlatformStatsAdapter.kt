package com.jeeps.gamecollector.remaster.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jeeps.gamecollector.remaster.ui.adapters.PlatformStatsAdapter.PlatformViewHolder
import com.jeeps.gamecollector.databinding.PlatformStatsItemLayoutBinding
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats
import com.jeeps.gamecollector.remaster.utils.extensions.completionPercentage
import com.jeeps.gamecollector.remaster.utils.extensions.totalGames

class PlatformStatsAdapter(
    private val platformsStats: List<PlatformStats>
) : RecyclerView.Adapter<PlatformViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatformViewHolder {
        val binding = PlatformStatsItemLayoutBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return PlatformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatformViewHolder, position: Int) {
        val platformsStats = platformsStats[position]
        holder.bind(platformsStats)
    }

    override fun getItemCount(): Int {
        return platformsStats.size
    }

    inner class PlatformViewHolder(private val binding: PlatformStatsItemLayoutBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(stats: PlatformStats) {
            with(binding) {
                platformName.text = stats.platformName
                platformTotal.text = stats.totalGames().toString()
                platformCompleted.text = stats.completedGamesTotal.toString()
                platformTotalPhysical.text = stats.physicalTotal.toString()
                platformTotalDigital.text = stats.digitalTotal.toString()
                platformCompletionPercentage.text = String.format("%d%%", stats.completionPercentage())
                platformLastGameCompleted.text = stats.lastGameCompleted

                val colors = intArrayOf(Color.parseColor("#FF5722"))
                platformCompletionChart.donutColors = colors
                platformCompletionChart.animation.duration = 1000L
                platformCompletionChart.animate(listOf(stats.completionPercentage().toFloat()))
            }
        }
    }
}