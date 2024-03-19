package com.jeeps.gamecollector.remaster.ui.userStats

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.compose.AppTheme
import com.google.android.material.snackbar.Snackbar
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityStatsBinding
import com.jeeps.gamecollector.databinding.ContentStatsBinding
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats
import com.jeeps.gamecollector.remaster.data.model.data.user.UserStats
import com.jeeps.gamecollector.remaster.ui.adapters.PlatformStatsAdapter
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.composables.CircularGraph
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
        val platformsStatsAdapter = PlatformStatsAdapter(platformStats)
        content.platformStatsRecyclerview.adapter = platformsStatsAdapter
    }

}

@Composable
fun UserStatsScreen(
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .background(Color.White)
            .padding(all = 10.dp)
            .fillMaxSize()
    ) {
        StatCard {
            Text(
                text = "Overall",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge,
            )
            CircularGraph(
                percentage = 1f,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    value = "100",
                    label = "Total",
                    modifier = Modifier
                        .weight(50f)
                )
                StatChip(
                    value = "33",
                    label = "Completed",
                    modifier = Modifier
                        .weight(50f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    value = "55",
                    label = "Physical",
                    modifier = Modifier
                        .weight(50f)
                )
                StatChip(
                    value = "45",
                    label = "Digital",
                    modifier = Modifier
                        .weight(50f)
                )
            }
            StatChip(
                value = "The legend of Zelda",
                label = "Last Game Completed",
                valueFontSize = 20.sp
            )

        }
    }
}

@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueFontSize: TextUnit = 30.sp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(percent = 50)
            )
    ) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = valueFontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp, start = 18.dp, end = 18.dp)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
fun StatCard(
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 20.dp)
        )
    }
}

@Preview
@Composable
private fun UserStatsScreenPreview() {
    AppTheme {
        UserStatsScreen()
    }
}