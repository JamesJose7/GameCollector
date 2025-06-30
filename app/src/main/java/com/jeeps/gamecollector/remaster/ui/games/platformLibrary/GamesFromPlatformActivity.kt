package com.jeeps.gamecollector.remaster.ui.games.platformLibrary

import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityPlatformLibraryBinding
import com.jeeps.gamecollector.databinding.ContentPlatformLibraryBinding
import com.jeeps.gamecollector.deprecated.utils.ColorsUtils
import com.jeeps.gamecollector.deprecated.utils.PlatformCovers
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.composables.Dialog
import com.jeeps.gamecollector.remaster.ui.composables.LoadingAnimation
import com.jeeps.gamecollector.remaster.ui.games.details.GameDetailsActivity
import com.jeeps.gamecollector.remaster.ui.games.edit.AddGameActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.AdvancedFiltersDialog
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.FilterStats
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getAppropriateComparator
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getFilterData
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getInfoData
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.showSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.jeeps.gamecollector.remaster.utils.extensions.withExclusions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import java.text.DecimalFormat

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GamesFromPlatformActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPlatformLibraryBinding::inflate)
    private lateinit var content: ContentPlatformLibraryBinding

    private val viewModel: GamesFromPlatformViewModel by viewModels()

    private val addGameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleAddGameResult(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.exitTransition = Fade().withExclusions()
        window.enterTransition = Fade().withExclusions()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        content = binding.content

        getIntentData()

        binding.screenCompose.setComposable {
            GamesFromPlatformScreen(
                viewModel = viewModel,
                onBackPressed = {
                    onBackPressedDispatcher.onBackPressed()
                },
                onEditGame = { game ->
                    val intent = Intent(this, GameDetailsActivity::class.java).apply {
                        putExtra(CURRENT_PLATFORM, viewModel.platformId)
                        putExtra(CURRENT_PLATFORM_NAME, viewModel.platformName)
                        putExtra(SELECTED_GAME, game)
                    }
                    // TODO: Make this transition when implementing Compose navigation with shared elements
                    /*val activityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(
                            this,
                            Pair.create(imageView, "cover"),
                            Pair.create(binding.fab, "fab")
                        )*/
                    addGameResultLauncher.launch(intent)
                },
                onAddGame = {
                    val intent = Intent(this, AddGameActivity::class.java).apply {
                        putExtra(CURRENT_PLATFORM, viewModel.platformId)
                        putExtra(CURRENT_PLATFORM_NAME, viewModel.platformName)
                    }
                    addGameResultLauncher.launch(intent)
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.deleteGamePendingDeletion()
    }

    private fun getIntentData() {
        intent.getStringExtra(CURRENT_PLATFORM)?.let { viewModel.platformId = it }
        intent.getStringExtra(CURRENT_PLATFORM_NAME)?.let { viewModel.platformName = it }
    }

    private fun handleAddGameResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra(ADD_GAME_RESULT_MESSAGE)?.let { message ->
                showSnackBar(binding.root, message)
            }
        }
    }

    companion object {
        const val CURRENT_PLATFORM = "CURRENT_PLATFORM"
        const val CURRENT_PLATFORM_NAME = "CURRENT_PLATFORM_NAME"
        const val SELECTED_GAME = "SELECTED_GAME"
        const val SELECTED_GAME_POSITION = "SELECTED_GAME_POSITION"
        const val ADD_GAME_RESULT_MESSAGE = "ADD_GAME_RESULT_MESSAGE"
    }
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GamesFromPlatformScreen(
    viewModel: GamesFromPlatformViewModel = viewModel(),
    onBackPressed: () -> Unit,
    onEditGame: (Game) -> Unit,
    onAddGame: () -> Unit
) {
    val games by viewModel.games.observeAsState(emptyList())
    val sortStat by viewModel.currentSortStat.observeAsState(SortStat.NONE)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredStats by viewModel.filteredStats.observeAsState(FilterStats())
    val filterControls by viewModel.currentFilterControls.collectAsState()
    val sortControls by viewModel.currentSortControls.collectAsState()
    val showInfoControls by viewModel.currentShowInfoControls.collectAsState()
    var showDeleteGameDialog by remember { mutableStateOf(false) }
    var gamePendingDeletion: Game? by remember { mutableStateOf(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filtersSheetState = rememberModalBottomSheetState()

    var showFiltersBottomSheet by remember { mutableStateOf(false) }

    val promptDeleteUndoAction: (game: Game) -> Unit = { game ->
        scope.launch {
            viewModel.removeGameLocally(game)
            var dismissedCalled = false
            try {
                val result = snackbarHostState
                    .showSnackbar(
                        message = "Deleted: ${game.name}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        dismissedCalled = true
                        viewModel.addGameLocally(game)
                    }

                    SnackbarResult.Dismissed -> {
                        dismissedCalled = true
                        viewModel.deleteGame(game)
                    }
                }
            } finally {
                // TODO: Replace this with a restore game functionality, so that the game is always deleted,
                //  and the user can undo the deletion by restoring the game instead
                // Called if a coroutine is cancelled, like when the user navigates back
                if (!dismissedCalled) {
                    viewModel.deleteGame(game)
                }
            }
        }
    }

    GamesFromPlatformScreen(
        snackbarHostState = snackbarHostState,
        games = games,
        platformName = viewModel.platformName,
        sortStat = sortStat,
        searchQuery = searchQuery,
        filteredStats = filteredStats,
        onBackPressed = onBackPressed,
        onAdvancedFiltersClicked = { showFiltersBottomSheet = true },
        onSearchQueryChanged = {
            viewModel.handleSearch(it)
        },
        onEditGame = onEditGame,
        onDeleteGame = {
            gamePendingDeletion = it
            showDeleteGameDialog = true
        },
        onAddGame = onAddGame
    )

    when {
        showDeleteGameDialog -> {
            gamePendingDeletion?.let { game ->
                Dialog(
                    title = "Delete game",
                    description = "Are you sure you want to delete ${game.name}",
                    icon = Icons.Filled.DeleteForever,
                    confirmButtonText = "Yes",
                    dismissButtonText = "No",
                    onDismissRequest = {
                        showDeleteGameDialog = false
                    },
                    onConfirmation = {
                        showDeleteGameDialog = false
                        promptDeleteUndoAction(game)
                    }
                )
            }
        }
        showFiltersBottomSheet -> {
            ModalBottomSheet(
                sheetState = filtersSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                onDismissRequest = { showFiltersBottomSheet = false }
            ) {
                AdvancedFiltersDialog(
                    filterControls = filterControls,
                    sortControls = sortControls,
                    showInfoControls = showInfoControls,
                    onFilterControlsUpdated = { filterControls ->
                        val (filtersList) = filterControls.getFilterData()
                        viewModel.setFilterControls(filterControls)
                        viewModel.updateFilters(filtersList)
                    },
                    onClearFilters = {
                        viewModel.clearFilters(true)
                    },
                    onSortControlsUpdated = { sortControls, isOrderSort ->
                        if (!isOrderSort) {
                            viewModel.clearShowInfoControls()
                        }

                        val (comparator, sort) = sortControls.getAppropriateComparator()
                        viewModel.setSortControls(sortControls)
                        viewModel.setCurrentSortStat(sort)
                        viewModel.rearrangeGames(comparator)
                    },
                    onShowInfoControlsUpdated = { showInfoControls ->
                        val (sort) = showInfoControls.getInfoData()
                        viewModel.clearShowInfoControls()
                        viewModel.setShowInfoControls(showInfoControls)
                        viewModel.setCurrentSortStat(sort)
                    },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun GamesFromPlatformScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    games: List<Game>,
    platformName: String,
    sortStat: SortStat,
    searchQuery: String,
    filteredStats: FilterStats,
    onBackPressed: () -> Unit = {},
    onAdvancedFiltersClicked: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onEditGame: (Game) -> Unit = {},
    onDeleteGame: (Game) -> Unit = {},
    onAddGame: () -> Unit = {}
) {
    val collapsingScaffoldState = rememberCollapsingToolbarScaffoldState()
    val platformCover = PlatformCovers.getPlatformCover(platformName)

    var showSearch by remember { mutableStateOf(false) }

    // Animate filter stats card when scrolling
    val gridState = rememberLazyGridState()
    val isScrolledFromTop by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
        }
    }
    val animatedPadding by animateDpAsState(
        targetValue = if (isScrolledFromTop) 12.dp else 60.dp,
        animationSpec = tween(durationMillis = 300),
        label = "filteredStatsAnimation"
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGame,
                shape = CircleShape,
                containerColor = colorResource(id = R.color.colorAccent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 0.dp)
    ) { innerPadding ->
        CollapsingToolbarScaffold(
            state = collapsingScaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            modifier = Modifier
                .fillMaxSize(),
            toolbar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .parallax(0.5f)
                ) {
                    Image(
                        painter = painterResource(platformCover),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    Spacer(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 1 - collapsingScaffoldState.toolbarState.progress))
                            .fillMaxSize()
                    )
                }

                // Collapsed toolbar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(56.dp)
                        .fillMaxWidth()
                        .pin()
                ) {
                    IconButton(
                        onClick = {
                            if (showSearch) {
                                showSearch = false
                            } else {
                                onBackPressed()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                    if (collapsingScaffoldState.toolbarState.progress == 0f && !showSearch) {
                        Text(
                            text = platformName,
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    if (showSearch) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    text = "Search...",
                                    color = Color.White
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                    IconButton(
                        onClick = {
                            // Clear search when closing
                            if (showSearch) {
                                onSearchQueryChanged("")
                            }
                            showSearch = !showSearch
                        }
                    ) {
                        Icon(
                            imageVector = if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = onAdvancedFiltersClicked
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
            },
        ) {
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 64.dp)
                ) {
                    items(items = games, key = { it.id }) { game ->
                        GameCard(
                            game = game,
                            sortStat = sortStat,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = { onEditGame(game) },
                                    onLongClick = { onDeleteGame(game) }
                                )
                        )
                    }
                }

                if (filteredStats.showStats) {
                    FilteredStatsCard(
                        stats = filteredStats,
                        modifier = Modifier
                            .padding(top = animatedPadding)
                            .align(Alignment.TopEnd)
                    )
                }
            }

        }
    }
}

@Composable
private fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    sortStat: SortStat
) {
    var isLoadingImage by remember { mutableStateOf(true) }
    val checkmarkColor = if (game.timesCompleted > 0) {
        Color(0xff7FFF00)
    } else {
        Color.LightGray
    }
    val hours = when (sortStat) {
        SortStat.HOURS_MAIN -> game.gameHoursStats.gameplayMain
        SortStat.HOURS_MAIN_EXTRA -> game.gameHoursStats.gameplayMainExtra
        SortStat.HOURS_COMPLETIONIST -> game.gameHoursStats.gameplayCompletionist
        SortStat.NONE -> 0.0
    }
    val decimalFormat = DecimalFormat("#.#")
    val hoursFormatted = stringResource(R.string.hours_template, decimalFormat.format(hours))

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray
        ),
        modifier = modifier
            .height(310.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = game.imageUri,
                    contentDescription = stringResource(id = R.string.game_cover),
                    contentScale = ContentScale.Fit,
                    onLoading = { isLoadingImage = true },
                    onSuccess = { isLoadingImage = false },
                    onError = { isLoadingImage = false },
                    error = painterResource(R.drawable.game_controller),
                    modifier = modifier
                        .fillMaxWidth()
                        .height(255.dp)
                        .background(color = Color.LightGray)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 8.dp)
                ) {
                    BasicText(
                        text = game.shortName.ifEmpty { game.name },
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 14.sp,
                            maxFontSize = 18.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colorResource(R.color.textColorPrimary)
                        ),
                        modifier = Modifier
                            .weight(85f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.checked),
                        contentDescription = "Is completed",
                        tint = checkmarkColor,
                        modifier = Modifier
                            .weight(15f)
                    )
                }
            }

            if (isLoadingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(255.dp)
                ) {
                    LoadingAnimation(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            if (!game.isPhysical) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Color(0xff555555))
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cloud),
                        contentDescription = "Is digital",
                        tint = Color.White,
                        modifier = modifier
                            .fillMaxWidth()
                            .height(255.dp)
                    )
                }
            }

            if (sortStat != SortStat.NONE) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 4.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 4.dp
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = hoursFormatted,
                        color = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilteredStatsCard(
    modifier: Modifier = Modifier,
    stats: FilterStats
) {
    val statsText = pluralStringResource(R.plurals.filtered_stats, stats.totalAmount, stats.filteredAmount, stats.totalAmount)

    Card(
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 0.dp, bottomStart = 10.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
    ) {
        Text(
            text = statsText,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Preview
@Composable
private fun GamesFromPlatformScreenPreview() {
    val games = List(10) {
        Game(
            id = it.toString(),
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
    }
    val filterStats = FilterStats(
        showStats = true,
        filteredAmount = 3,
        totalAmount = 10
    )

    AppTheme {
        GamesFromPlatformScreen(
            snackbarHostState = SnackbarHostState(),
            games = games,
            platformName = "Nintendo Switch",
            sortStat = SortStat.HOURS_MAIN,
            searchQuery = "",
            filteredStats = filterStats
        )
    }
}

@Preview(device = "spec:width=1080px,height=600px,dpi=440,orientation=portrait")
@Composable
private fun GameCardPreview() {
    val game = Game(
        name = "The Legend of Zelda: Breath of the Wild",
        shortName = "Zelda: BOTW",
        imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r76.jpg",
        isPhysical = false,
        timesCompleted = 1,
        gameHoursStats = GameHoursStats(
            gameplayMain = 50.0,
            gameplayMainExtra = 90.0,
            gameplayCompletionist = 180.0
        )
    )
    AppTheme {
        GameCard(
            game = game,
            sortStat = SortStat.HOURS_MAIN
        )
    }
}

@Preview(device = "spec:width=1080px,height=600px,dpi=440,orientation=portrait")
@Composable
private fun GameCardNoSortStatPreview() {
    val game = Game(name = "The Witcher 3: Wild Hunt")
    AppTheme {
        GameCard(game = game, sortStat = SortStat.NONE)
    }
}