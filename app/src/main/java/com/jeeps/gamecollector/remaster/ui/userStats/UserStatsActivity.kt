package com.jeeps.gamecollector.remaster.ui.userStats

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.adapters.PlatformStatsAdapter
import com.jeeps.gamecollector.databinding.ActivityStatsBinding
import com.jeeps.gamecollector.databinding.ContentStatsBinding
import com.jeeps.gamecollector.model.PlatformStats
import com.jeeps.gamecollector.model.UserStats
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.completionPercentage
import com.jeeps.gamecollector.remaster.utils.extensions.totalGames
import com.jeeps.gamecollector.remaster.utils.extensions.value
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UserStatsActivity : BaseActivity() {

    private val binding by viewBinding(ActivityStatsBinding::inflate).also {
        // Workaround from view not initializing properly when view binding
        if (it.isInitialized()) {
            it.value.content.overallCompletionChart.show(listOf())
        }
    }
    private lateinit var content: ContentStatsBinding

    private val viewModel: UserStatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Statistics"
        content = binding.content

        initializeCharts()
        initializePlatformsRv()
        initializeObservers()
    }

    private fun initializeCharts() {
        val colors = intArrayOf(getColor(R.color.colorAccent))
        content.overallCompletionChart.donutColors = colors
        content.overallCompletionChart.animation.duration = 1000L
        content.overallCompletionChart.animate(listOf(0f))
    }

    private fun initializePlatformsRv() {
        val platformsRv = content.platformStatsRecyclerview
        val layoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        platformsRv.layoutManager = layoutManager
        platformsRv.setHasFixedSize(true)
    }

    private fun initializeObservers() {
        viewModel.isLoading.observe(this) {
            content.statsProgressBar.visibility = if (it.value()) View.VISIBLE else View.GONE
        }

        viewModel.userStats.observe(this) {
            it?.let {
                bindUserStats(it)
            }
        }
    }

    private fun bindUserStats(userStats: UserStats) {
        if (userStats.platformStats.isEmpty()) {
            Snackbar.make(
                    binding.root,
                    "You don't have any stats yet, Start by creating a platform",
                    Snackbar.LENGTH_SHORT
                )
                .show()
            return
        }

        // Overall stats
        val completionPercentageText = "${userStats.completionPercentage()}%"
        content.overallCompletionPercentage.text = completionPercentageText
        content.overallTotal.text = userStats.totalGames().toString()
        content.overallCompleted.text = userStats.completedGamesTotal.toString()
        content.overallTotalPhysical.text = userStats.physicalTotal.toString()
        content.overallTotalDigital.text = userStats.digitalTotal.toString()
        content.overallLastGameCompleted.text = userStats.lastGameCompleted
        // Chart
        content.overallCompletionChart.animate(listOf(userStats.completionPercentage().toFloat()))

        // Platform stats
        setPlatformsAdapter(userStats.platformStats)
    }

    private fun setPlatformsAdapter(platformStats: List<PlatformStats>) {
        val platformsStatsAdapter =
            PlatformStatsAdapter(this, platformStats)
        content.platformStatsRecyclerview.adapter = platformsStatsAdapter
    }

}