@file:OptIn(ExperimentalCoroutinesApi::class)

package com.jeeps.gamecollector.remaster.ui.games.details

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.releaseDateFormatted
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.ui.composables.CompletionTimeline
import com.jeeps.gamecollector.remaster.ui.composables.FireworksAnimation
import com.jeeps.gamecollector.remaster.ui.composables.HourStats
import com.jeeps.gamecollector.remaster.ui.composables.RatingChip
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel = hiltViewModel(),
    platformId: String? = null,
    platformName: String? = null,
    selectedGame: Game? = null,
    onBackPressed: () -> Unit = {},
    onEditGame: (Game) -> Unit = {}
) {
    val game by viewModel.selectedGame.observeAsState(Game())
    val platformGames by viewModel.games.observeAsState(emptyList())
    val stats by viewModel.gameHoursStats.observeAsState(GameplayHoursStats())
    val isLoadingHourStats by viewModel.loadingGameHours.observeAsState(true)
    val isLoadingCompletionUpdate by viewModel.loadingCompletionUpdate.observeAsState(false)
    val isError by viewModel.showHoursErrorMessage.observeAsState(false)
    val gameMainColor by viewModel.gameMainColor.observeAsState(MaterialTheme.colorScheme.primary)

    val activity = LocalActivity.current
    val window = activity?.window
    val view = LocalView.current

    val topBarColor by animateColorAsState(
        targetValue = gameMainColor,
        animationSpec = tween(durationMillis = 300)
    )
    val useDarkIcons = remember(gameMainColor) {
        val luminance = ColorUtils.calculateLuminance(gameMainColor.toArgb())
        luminance > 0.5
    }
    val topBarTextColor = remember(gameMainColor) {
        val luminance = ColorUtils.calculateLuminance(gameMainColor.toArgb())
        if (luminance < 0.5) Color.White else Color(0xFF212121)
    }

    LaunchedEffect(platformId, platformName, selectedGame) {
        viewModel.platformId = platformId.orEmpty()
        viewModel.platformName = platformName
        selectedGame?.let { viewModel.setSelectedGame(it) }
    }

    DisposableEffect(useDarkIcons) {
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousValue = insetsController?.isAppearanceLightStatusBars

        insetsController?.isAppearanceLightStatusBars = useDarkIcons

        onDispose {
            // Return the color to previous value so that other screens won't remain with the wrong color
            insetsController?.isAppearanceLightStatusBars = previousValue ?: true
        }
    }

    GameDetailsScreen(
        game = game,
        platformGames = platformGames,
        hoursStats = stats,
        isLoadingStats = isLoadingHourStats,
        isLoadingCompletionUpdate = isLoadingCompletionUpdate,
        isStatsError = isError,
        topBarColor = topBarColor,
        topBarTextColor = topBarTextColor,
        onRefreshClick = { viewModel.getGameHours() },
        onGameCompletedClick = { viewModel.updateGameCompletion() },
        onBackPressed = onBackPressed,
        onEditGame = onEditGame
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    modifier: Modifier = Modifier,
    game: Game,
    platformGames: List<Game>,
    onGameCompletedClick: () -> Unit = {},
    hoursStats: GameplayHoursStats,
    isLoadingStats: Boolean,
    isLoadingCompletionUpdate: Boolean,
    isStatsError: Boolean,
    topBarColor: Color = MaterialTheme.colorScheme.primary,
    topBarTextColor: Color = Color.White,
    onRefreshClick: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onEditGame: (Game) -> Unit = {}
) {
    var isCompletedButtonClicked by rememberSaveable { mutableStateOf(false) }
    val title = game.shortName.ifEmpty { game.name }
    val isComplete = game.timesCompleted > 0
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.title_activity_game_details),
                        color = topBarTextColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description",
                            tint = topBarTextColor
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditGame(game) },
                shape = CircleShape,
                containerColor = colorResource(id = R.color.colorAccent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    ) { innerPadding ->
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(bottom = 80.dp)
        ) {
            val (header, completedButton, lottieAnimation, details) = createRefs()
            val middleGuideline = createGuidelineFromStart(0.4f)

            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .constrainAs(header) {
                        top.linkTo(parent.top, margin = 10.dp)
                    }
            ) {
                AsyncImage(
                    model = game.imageUri,
                    contentDescription = stringResource(id = R.string.game_cover),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(150.dp)
                        .height(200.dp)
                        .shadow(elevation = 5.dp)
                        .background(color = Color.LightGray)
                )
                Column(
                    modifier = Modifier
                        .height(200.dp)
                        .padding(start = 10.dp)
                        .overscroll(null)
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
                    Text(
                        text = game.platform,
                        fontSize = 17.sp,
                        color = colorResource(id = R.color.textSecondaryColor),
                        modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color(0x555e5e5e)
                    )
                    Text(
                        text = stringResource(id = R.string.released_in),
                        fontSize = 11.sp,
                        color = colorResource(id = R.color.textSecondaryColor),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = game.releaseDateFormatted(),
                        fontSize = 17.sp,
                        color = colorResource(id = R.color.textSecondaryColor)
                    )
                    if (game.genresNames.isNotEmpty()) {
                        GenresChips(
                            genres = game.genresNames,
                            modifier = Modifier
                                .padding(top = 4.dp)
                        )
                    }
                }
            }

            if ((isComplete && isCompletedButtonClicked) && !isLoadingCompletionUpdate) {
                FireworksAnimation(
                    animationReps = 2,
                    modifier = Modifier
                        .size(200.dp)
                        .constrainAs(lottieAnimation) {
                            bottom.linkTo(completedButton.top)
                            start.linkTo(middleGuideline)
                            end.linkTo(parent.end)
                        }
                )
            }

            CompletedButton(
                game = game,
                isLoadingCompletionUpdate = isLoadingCompletionUpdate,
                onGameCompletedClick = {
                    onGameCompletedClick()
                    isCompletedButtonClicked = true
                },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .constrainAs(completedButton) {
                        top.linkTo(header.bottom, margin = 16.dp)
                    }
            )

            Column(
                modifier = modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .constrainAs(details) {
                        top.linkTo(completedButton.bottom)
                    }
            ) {
                HourStatsCardContent(
                    hoursStats = hoursStats,
                    isLoadingStats = isLoadingStats,
                    isError = isStatsError,
                    onRefreshClick = onRefreshClick,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 10.dp)
                )
                GameRatingsCardContent(
                    game = game,
                    modifier = Modifier
                        .padding(all = 10.dp)
                )
                if (platformGames.isNotEmpty() && game.timesCompleted > 0) {
                    GameTimelineCardContent(
                        games = platformGames,
                        selectedGame = game,
                        modifier = Modifier
                            .padding(all = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GenresChips(
    modifier: Modifier = Modifier,
    genres: List<String>
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        genres.map { genre ->
            Text(
                text = genre,
                fontSize = 11.sp,
                color = colorResource(R.color.textSecondaryColor),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun CompletedButton(
    modifier: Modifier = Modifier,
    game: Game,
    isLoadingCompletionUpdate: Boolean,
    onGameCompletedClick: () -> Unit = {}
) {
    val isComplete = game.timesCompleted > 0
    val buttonTitle = if (isComplete) { R.string.completed } else { R.string.unfinished }
    val backgroundColor = if (isComplete) { R.color.success_darker } else { R.color.inactive_darker }
    val rippleColor = if (!isComplete) { R.color.success_darker } else { R.color.inactive_darker }
    val elevation = if (isComplete) { 10.dp } else { 1.dp }

    ElevatedButton(
        onClick = {
            if (!isLoadingCompletionUpdate) {
                onGameCompletedClick()
            }
        },
        shape = RoundedCornerShape(size = 10.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = colorResource(id = backgroundColor),
            contentColor = colorResource(id = rippleColor)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoadingCompletionUpdate) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(25.dp)
            )
        } else {
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
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 18.sp,
        color = colorResource(id = R.color.textColorPrimary),
        modifier = modifier.fillMaxWidth()
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
fun GameRatingsCardContent(
    game: Game,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            SectionTitle(
                text = stringResource(id = R.string.ratings_title),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                RatingChip("Users", game.userRating, game.userRatingCount)
                RatingChip("Critics", game.criticsRating, game.criticsRatingCount)
                RatingChip("Total", game.totalRating, game.totalRatingCount)
            }
        }
    }
}

@Composable
fun GameTimelineCardContent(
    games: List<Game>,
    selectedGame: Game,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            SectionTitle(
                text = stringResource(id = R.string.timeline_title),
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .padding(horizontal = 12.dp)
            )
            CompletionTimeline(
                games = games,
                selectedGame = selectedGame,
                modifier = Modifier
                    .padding(vertical = 10.dp)
            )
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
        timesCompleted = 2,
        genresNames = listOf("RPG", "FPS", "MOBA"),
        completionDate = "2023-10-22T02:32:04.808Z"
    )
    val stats = GameplayHoursStats(
        gameplayMain = 50.0,
        gameplayMainExtra = 97.0,
        gameplayCompletionist = 88.0
    )
    AppTheme {
        GameDetailsScreen(
            game = game,
            platformGames = listOf(game),
            hoursStats = stats,
            isLoadingStats = false,
            isLoadingCompletionUpdate = false,
            isStatsError = false
        )
    }
}