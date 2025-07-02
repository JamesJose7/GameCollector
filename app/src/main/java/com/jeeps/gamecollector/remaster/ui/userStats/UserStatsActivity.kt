@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)

package com.jeeps.gamecollector.remaster.ui.userStats

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import com.jeeps.gamecollector.databinding.ActivityStatsBinding
import com.jeeps.gamecollector.remaster.data.model.data.platforms.PlatformStats
import com.jeeps.gamecollector.remaster.data.model.data.user.UserStats
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.composables.CircularGraph
import com.jeeps.gamecollector.remaster.utils.extensions.completionPercent
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.totalGames
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UserStatsActivity : BaseActivity() {

    private val binding by viewBinding(ActivityStatsBinding::inflate)

    private val viewModel: UserStatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.screenCompose.setComposable { UserStatsScreen(viewModel) }
    }
}

@Composable
fun UserStatsScreen(
    userStatsViewModel: UserStatsViewModel = hiltViewModel()
) {
    val userStats by userStatsViewModel.userStats.collectAsState()

    UserStatsScreen(userStats = userStats)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatsScreen(
    userStats: UserStats
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(text = "Statistics")
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 10.dp)
            ) {
                UserStatsCard(userStats = userStats)
                userStats.platformStats.forEach { platform ->
                    PlatformStatsCard(platformStats = platform)
                }
            }
        }
    }
}

@Composable
fun UserStatsCard(
    userStats: UserStats
) {
    StatCard {
        Text(
            text = "Overall",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge,
        )
        CircularGraph(
            percentage = userStats.completionPercent(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip(
                value = "${userStats.totalGames()}",
                label = "Total",
                modifier = Modifier
                    .weight(50f)
            )
            StatChip(
                value = "${userStats.completedGamesTotal}",
                label = "Completed",
                modifier = Modifier
                    .weight(50f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip(
                value = "${userStats.physicalTotal}",
                label = "Physical",
                modifier = Modifier
                    .weight(50f)
            )
            StatChip(
                value = "${userStats.digitalTotal}",
                label = "Digital",
                modifier = Modifier
                    .weight(50f)
            )
        }
        StatChip(
            value = userStats.lastGameCompleted,
            label = "Last Game Completed",
            valueFontSize = 20.sp
        )
    }
}

@Composable
fun PlatformStatsCard(
    platformStats: PlatformStats
) {
    StatCard {
        Text(
            text = platformStats.platformName,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge,
        )
        CircularGraph(
            percentage = platformStats.completionPercent(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip(
                value = "${platformStats.totalGames()}",
                label = "Total",
                modifier = Modifier
                    .weight(50f)
            )
            StatChip(
                value = "${platformStats.completedGamesTotal}",
                label = "Completed",
                modifier = Modifier
                    .weight(50f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip(
                value = "${platformStats.physicalTotal}",
                label = "Physical",
                modifier = Modifier
                    .weight(50f)
            )
            StatChip(
                value = "${platformStats.digitalTotal}",
                label = "Digital",
                modifier = Modifier
                    .weight(50f)
            )
        }
        StatChip(
            value = platformStats.lastGameCompleted,
            label = "Last Game Completed",
            valueFontSize = 20.sp
        )

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
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = valueFontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp, start = 18.dp, end = 18.dp)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(horizontal = 10.dp)
                .padding(top = 10.dp, bottom = 20.dp)
        )
    }
}

@Preview
@Composable
private fun UserStatsScreenPreview() {
    val userStats = UserStats(
        physicalTotal = 44,
        digitalTotal = 20,
        completedGamesTotal = 44,
        lastGameCompleted = "The Legend of Zelda",
        platformStats = listOf(
            PlatformStats(
                platformName = "Switch",
                physicalTotal = 20,
                digitalTotal = 10,
                completedGamesTotal = 15,
                lastGameCompleted = "Metroid"
            ),
            PlatformStats(
                platformName = "Wii",
                physicalTotal = 44,
                digitalTotal = 12,
                completedGamesTotal = 23,
                lastGameCompleted = "Mario"
            )
        )
    )

    AppTheme {
        UserStatsScreen(userStats)
    }
}