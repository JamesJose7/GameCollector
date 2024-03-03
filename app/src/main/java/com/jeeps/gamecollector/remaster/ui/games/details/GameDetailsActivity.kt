@file:OptIn(ExperimentalCoroutinesApi::class)

package com.jeeps.gamecollector.remaster.ui.games.details

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityGameDetailsBinding
import com.jeeps.gamecollector.databinding.ContentGameDetailsBinding
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.releaseDateFormatted
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

                updateGameCompletedButton(game)
                setupCompleteSwitch()

                getCoverColors()
            }
        }

        content.screenCompose.setComposable { GameDetailsScreen(viewModel) }
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


@Composable
fun GameDetailsScreen(
    gameDetailsViewModel: GameDetailsViewModel = viewModel()
) {
    val game by gameDetailsViewModel.selectedGame.observeAsState(Game())
    val stats by gameDetailsViewModel.gameHoursStats.observeAsState(GameplayHoursStats())
    val isLoading by gameDetailsViewModel.loadingGameHours.observeAsState(true)
    val isError by gameDetailsViewModel.showHoursErrorMessage.observeAsState(false)

    GameDetailsScreen(
        game = game,
        hoursStats = stats,
        isLoadingStats = isLoading,
        isStatsError = isError,
        onRefreshClick = { gameDetailsViewModel.getGameHours() },
        onGameCompletedClick = { gameDetailsViewModel.updateGameCompletion() }
    )
}
@Composable
fun GameDetailsScreen(
    modifier: Modifier = Modifier,
    game: Game,
    onGameCompletedClick: () -> Unit = {},
    hoursStats: GameplayHoursStats,
    isLoadingStats: Boolean,
    isStatsError: Boolean,
    onRefreshClick: () -> Unit = {}
) {
    val title = game.shortName.ifEmpty { game.name }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, top = 10.dp)
        ) {
            Spacer(modifier = Modifier
                .width(150.dp)
                .height(200.dp))
            Column(
                modifier = Modifier
                    .height(200.dp)
                    .padding(start = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = title, fontSize = 19.sp, color = colorResource(id = R.color.textColorPrimary), modifier = Modifier.padding(bottom = 1.dp))
                if (game.publisher.isNotEmpty()) {
                    Text(
                        text = game.publisher,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.textSecondaryColor)
                    )
                }
                Text(text = game.platform, fontSize = 17.sp, color = colorResource(id = R.color.textSecondaryColor), modifier = Modifier.padding(top = 10.dp, bottom = 5.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color(0x555e5e5e), modifier = Modifier.padding(bottom = 5.dp))
                Text(text = stringResource(id = R.string.released_in), fontSize = 11.sp, color = colorResource(id = R.color.textSecondaryColor), modifier = Modifier.padding(top = 10.dp))
                Text(text = game.releaseDateFormatted(), fontSize = 17.sp, color = colorResource(id = R.color.textSecondaryColor))
            }
        }
        CompletedButton(
            game = game,
            onGameCompletedClick = onGameCompletedClick,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 10.dp)
        )
        SectionTitle(text = stringResource(id = R.string.hours_stats),
            modifier
                .padding(horizontal = 10.dp)
                .padding(top = 20.dp))
        HourStatsCardContent(
            hoursStats = hoursStats,
            isLoadingStats = isLoadingStats,
            isError = isStatsError,
            onRefreshClick = onRefreshClick,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .padding(top = 10.dp)
        )
        SectionTitle(text = stringResource(id = R.string.ratings_title),
            modifier
                .padding(horizontal = 10.dp)
                .padding(top = 20.dp)
        )
        GameRatingsCardContent(
            game = game, modifier = Modifier
                .padding(horizontal = 10.dp)
                .padding(top = 10.dp)
        )
    }
}

@Composable
fun CompletedButton(
    modifier: Modifier = Modifier,
    game: Game,
    onGameCompletedClick: () -> Unit = {}
) {
    val isComplete = game.timesCompleted > 0
    val buttonTitle = if (isComplete) { R.string.completed } else { R.string.unfinished }
    val backgroundColor = if (isComplete) { R.color.success_darker } else { R.color.inactive_darker }

    ElevatedButton(
        onClick = { onGameCompletedClick() },
        shape = RoundedCornerShape(size = 10.dp),
        colors = ButtonDefaults.elevatedButtonColors(containerColor = colorResource(id = backgroundColor)),
        // elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = buttonTitle),
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 20.sp,
        color = colorResource(id = R.color.textColorPrimary),
        modifier = modifier.fillMaxWidth()
    )
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
        hoursStats = stats,
        isLoadingStats = isLoading,
        isError = isError,
        onRefreshClick = { gameDetailsViewModel.getGameHours() }
    )
}

@Composable
fun HourStatsCardContent(
    hoursStats: GameplayHoursStats,
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
            storyHours = hoursStats.gameplayMain,
            mainExtraHours = hoursStats.gameplayMainExtra,
            completionistHours = hoursStats.gameplayCompletionist,
            isLoadingStats = isLoadingStats,
            isError = isError,
            onRefreshClick = onRefreshClick
        )
    }
}

@Composable
fun GameRatingsCard(
    gameDetailsViewModel: GameDetailsViewModel = viewModel()
) {
    val selectedGame by gameDetailsViewModel.selectedGame.observeAsState(Game())

    GameRatingsCardContent(
        game = selectedGame
    )
}

@Composable
fun GameRatingsCardContent(
    game: Game,
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
            RatingChip("Users", game.userRating, game.userRatingCount)
            RatingChip("Critics", game.criticsRating, game.criticsRatingCount)
            RatingChip("Total", game.totalRating, game.totalRatingCount)
        }
    }
}

@Preview
@Composable
fun GameDetailsPreview() {
    val game = Game(
        name = "The Legend of Zelda",
        publisher = "Nintendo",
        platform = "Nintendo Switch",
        firstReleaseDate = 509366025,
        userRating = 10.0,
        userRatingCount = 10,
        criticsRating = 50.0,
        criticsRatingCount = 213,
        totalRating = 90.0,
        totalRatingCount = 223,
        timesCompleted = 2
    )
    val stats = GameplayHoursStats(
        gameplayMain = 50.0,
        gameplayMainExtra = 97.0,
        gameplayCompletionist = 88.0
    )
    AppTheme {
        GameDetailsScreen(
            game = game,
            hoursStats = stats,
            isLoadingStats = false,
            isStatsError = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HourStatsPreview() {
    val stats = GameplayHoursStats(
        gameplayMain = 50.0,
        gameplayMainExtra = 97.0,
        gameplayCompletionist = 88.0
    )
    AppTheme {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            HourStatsCardContent(
                hoursStats = stats,
                isLoadingStats = false,
                isError = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameRatingsPreview() {
    val game = Game(
        userRating = 10.0,
        userRatingCount = 10,
        criticsRating = 50.0,
        criticsRatingCount = 213,
        totalRating = 90.0,
        totalRatingCount = 223
    )
    AppTheme {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            GameRatingsCardContent(
                game = game
            )
        }
    }
}