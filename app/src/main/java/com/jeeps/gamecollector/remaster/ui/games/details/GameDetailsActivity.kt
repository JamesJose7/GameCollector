package com.jeeps.gamecollector.remaster.ui.games.details

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityGameDetailsBinding
import com.jeeps.gamecollector.databinding.ContentGameDetailsBinding
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.composables.HourStats
import com.jeeps.gamecollector.remaster.ui.composables.RatingChip
import com.jeeps.gamecollector.remaster.ui.games.edit.AddGameActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.ADD_GAME_RESULT_MESSAGE
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM_NAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME_POSITION
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.showSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.jeeps.gamecollector.remaster.utils.extensions.withExclusions
import com.squareup.picasso.Picasso
import com.varunest.sparkbutton.SparkEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GameDetailsActivity : BaseActivity() {

    private val binding by viewBinding(ActivityGameDetailsBinding::inflate)
    private lateinit var content: ContentGameDetailsBinding

    private val viewModel: GameDetailsViewModel by viewModels()

    private val editGameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.let { handleEditGameResult(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup exit transition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.enterTransition = Fade().withExclusions()
        window.exitTransition = Fade().withExclusions()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        super.onCreate(savedInstanceState)
        setContentView(binding.rootLayout)
        setSupportActionBar(binding.toolbar)
        content = binding.content.apply {
            gameDetailsViewModel = viewModel
            lifecycleOwner = this@GameDetailsActivity
        }

        getIntentData()

        bindViews()
        bindFab()
        bindAlerts()
    }

    private fun getIntentData() {
        viewModel.platformId = intent.getStringExtra(CURRENT_PLATFORM)
        viewModel.platformName = intent.getStringExtra(CURRENT_PLATFORM_NAME)
        viewModel.setSelectedGame(intent.getSerializableExtra(SELECTED_GAME) as Game)
        viewModel.selectedGamePosition = intent.getIntExtra(SELECTED_GAME_POSITION, -1)
    }

    private fun bindViews() {
        viewModel.selectedGame.observe(this) {
            it?.let { game ->
                game.currentSortStat = ""
                if (game.imageUri.isNotEmpty())
                    Picasso.get().load(game.imageUri).into(content.gameCover)
                val title = game.shortName.ifEmpty { game.name }
                content.gameTitle.text = title

                if (game.publisher.isEmpty())
                    content.gamePublisher.visibility = View.GONE
                else
                    content.gamePublisher.text = game.publisher
                content.gamePlatform.text = game.platform

                updateGameCompletedButton(game)
                setupCompleteSwitch()

                getCoverColors()
            }
        }

        content.ratingsCardCompose.setComposable { GameRatingsCard(viewModel) }
        content.gameHoursCompose.setComposable { HourStatsCard(viewModel) }
    }

    private fun updateGameCompletedButton(game: Game) = with(content) {
        val isComplete = game.timesCompleted > 0
        content.completeSwitch.isChecked = isComplete

        val backgroundColor = if (isComplete) {
            ContextCompat.getColor(root.context, R.color.success_darker)
        } else {
            ContextCompat.getColor(root.context, R.color.inactive_darker)
        }
        completedButtonBackground.setCardBackgroundColor(backgroundColor)
    }

    private fun bindFab() {
        binding.fab.setOnClickListener {
            editGame()
        }
    }

    private fun bindAlerts() {
        viewModel.errorMessage.observe(this) {
            it?.let { showToast(it) }
        }

        viewModel.serverMessage.observe(this) { messageEvent ->
            messageEvent?.getContentIfNotHandled()?.let {
                showSnackBar(binding.root, it)
            }
        }
    }

    private fun editGame() {
        val intent = Intent(this, AddGameActivity::class.java).apply {
            putExtra(CURRENT_PLATFORM, viewModel.platformId)
            putExtra(CURRENT_PLATFORM_NAME, viewModel.platformName)
            putExtra(SELECTED_GAME, viewModel.selectedGame.value)
            putExtra(SELECTED_GAME_POSITION, viewModel.selectedGamePosition)
        }
        editGameResultLauncher.launch(intent)
    }

    private fun handleEditGameResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val intent = Intent().apply {
                result.data?.getStringExtra(ADD_GAME_RESULT_MESSAGE)?.let { message ->
                    putExtra(ADD_GAME_RESULT_MESSAGE, message)
                }
            }
            setResult(result.resultCode, intent)
            finish()
        }
    }

    private fun setupCompleteSwitch() {
        content.completedButton.setOnClickListener {
            content.completeSwitch.performClick()
        }

        content.completeSwitch.setEventListener(object : SparkEventListener {
            override fun onEvent(button: ImageView?, buttonState: Boolean) {
                viewModel.updateGameCompletion()
            }

            override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

            override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}
        })
    }

    private fun getCoverColors() {
        viewModel.getColorBasedOnCover()
        viewModel.gameMainColor.observe(this) { color ->
            color?.let { animateStatusBarColor(it) }
        }
    }

    private fun animateStatusBarColor(colorTo: Int) {
        if (!viewModel.toolbarAnimationStarted) {
            viewModel.toolbarAnimationStarted = true
            val colorFrom = ContextCompat.getColor(this, R.color.colorPrimary)
            ObjectAnimator
                .ofObject(
                    binding.toolbar, "backgroundColor",
                    ArgbEvaluator(),
                    colorFrom,
                    colorTo
                )
                .setDuration(300)
                .start()
            window.statusBarColor = colorTo
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HourStatsCard(
    gameDetailsViewModel: GameDetailsViewModel = viewModel()
) {
    val stats by gameDetailsViewModel.gameHoursStats.observeAsState(GameplayHoursStats())
    val isLoading by gameDetailsViewModel.loadingGameHours.observeAsState(true)
    val isError by gameDetailsViewModel.showHoursErrorMessage.observeAsState(false)

    HourStatsCardContent(
        storyHours = stats.gameplayMain,
        mainExtraHours = stats.gameplayMainExtra,
        completionistHours = stats.gameplayCompletionist,
        isLoadingStats = isLoading,
        isError = isError,
        onRefreshClick = { gameDetailsViewModel.getGameHours() }
    )
}

@Composable
fun HourStatsCardContent(
    storyHours: Double,
    mainExtraHours: Double,
    completionistHours: Double,
    isLoadingStats: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        HourStats(
            storyHours = storyHours,
            mainExtraHours = mainExtraHours,
            completionistHours = completionistHours,
            isLoadingStats = isLoadingStats,
            isError = isError,
            onRefreshClick = onRefreshClick
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun GameRatingsCard(
    gameDetailsViewModel: GameDetailsViewModel = viewModel()
) {
    val selectedGame by gameDetailsViewModel.selectedGame.observeAsState(Game())

    GameRatingsCardContent(
        userRating = selectedGame.userRating,
        userRatingCount = selectedGame.userRatingCount,
        criticsRating = selectedGame.criticsRating,
        criticsRatingCount = selectedGame.criticsRatingCount,
        totalRating = selectedGame.totalRating,
        totalRatingCount = selectedGame.totalRatingCount
    )
}

@Composable
fun GameRatingsCardContent(
    userRating: Double,
    userRatingCount: Int,
    criticsRating: Double,
    criticsRatingCount: Int,
    totalRating: Double,
    totalRatingCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 10.dp)
        ) {
            RatingChip("Users", userRating, userRatingCount)
            RatingChip("Critics", criticsRating, criticsRatingCount)
            RatingChip("Total", totalRating, totalRatingCount)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HourStatsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            HourStatsCardContent(
                storyHours = 50.0,
                mainExtraHours = 97.0,
                completionistHours = 188.0,
                isLoadingStats = false,
                isError = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameRatingsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            GameRatingsCardContent(
                userRating = 10.0,
                userRatingCount = 10,
                criticsRating = 50.0,
                criticsRatingCount = 213,
                totalRating = 90.0,
                totalRatingCount = 223
            )
        }
    }
}