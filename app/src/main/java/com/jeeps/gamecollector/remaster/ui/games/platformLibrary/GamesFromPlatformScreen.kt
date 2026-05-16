package com.jeeps.gamecollector.remaster.ui.games.platformLibrary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.deprecated.utils.ColorsUtils
import com.jeeps.gamecollector.deprecated.utils.PlatformCovers
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.ui.composables.Dialog
import com.jeeps.gamecollector.remaster.ui.composables.LoadingAnimation
import com.jeeps.gamecollector.remaster.ui.composables.SharedElements
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformViewModel.GenreFilter
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.AdvancedFiltersDialog
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.FilterStats
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.ShowInfoControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.ShowStat
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.SortControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getShowStat
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GamesFromPlatformScreen(
    viewModel: GamesFromPlatformViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackPressed: () -> Unit,
    onEditGame: (Game) -> Unit,
    onAddGame: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
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
        animatedVisibilityScope = animatedVisibilityScope,
        snackbarHostState = snackbarHostState,
        games = state.games,
        platformName = state.platformName,
        sortStat = state.sortStat,
        sortControls = state.sortControls,
        showInfoControls = state.showInfoControls,
        searchQuery = state.searchQuery,
        filteredStats = state.filteredStats,
        genresFilters = state.genresFilterControls,
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
                    filterControls = state.filterControls,
                    sortControls = state.sortControls,
                    showInfoControls = state.showInfoControls,
                    genresFilterControls = state.genresFilterControls,
                    onFilterControlsUpdated = { filterControls ->
                        viewModel.setFilterControls(filterControls)
                    },
                    onClearFilters = {
                        viewModel.clearFiltersAndSort()
                    },
                    onSortControlsUpdated = { sortControls, isOrderSort ->
                        if (!isOrderSort) {
                            viewModel.clearShowInfoControls()
                        }

                        viewModel.setSortControls(sortControls)
                    },
                    onShowInfoControlsUpdated = { showInfoControls ->
                        viewModel.clearShowInfoControls()
                        viewModel.setShowInfoControls(showInfoControls)
                    },
                    onGenreFilterUpdated = { genreFilter ->
                        viewModel.updateGenreFilters(genreFilter)
                    },
                    modifier = Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GamesFromPlatformScreen(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    snackbarHostState: SnackbarHostState,
    games: List<Game>,
    platformName: String,
    sortStat: SortStat,
    sortControls: SortControls,
    genresFilters: List<GenreFilter>,
    showInfoControls: ShowInfoControls,
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
    val focusRequester = remember { FocusRequester() }
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

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

    // Reset scroll when sorting or filtering
    var previousSortStat by remember { mutableStateOf<SortStat?>(null) }
    var previousFilterStats by remember { mutableStateOf<FilterStats?>(null) }
    var previousGenresFilters by remember { mutableStateOf<List<GenreFilter>?>(null) }
    var previousSortControls by remember { mutableStateOf<SortControls?>(null) }

    LaunchedEffect(sortStat, filteredStats, sortControls, genresFilters) {
        // Track previous sort stat and filter stats to prevent changing when navigating back from another screen
        val sortChanged = previousSortStat != null && previousSortStat != sortStat
        val sortControlsChanged = previousSortControls != null && previousSortControls != sortControls
        val filterChanged = previousFilterStats != null && previousFilterStats != filteredStats
        val genresFiltersChanged = previousGenresFilters != null && previousGenresFilters != genresFilters

        if (sortChanged || filterChanged || sortControlsChanged || genresFiltersChanged) {
            gridState.scrollToItem(0)
        }

        previousSortStat = sortStat
        previousSortControls = sortControls
        previousFilterStats = filteredStats
        previousGenresFilters = genresFilters
    }

    // Request focus when search bar is visible
    LaunchedEffect(showSearch) {
        if (showSearch) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGame,
                shape = CircleShape,
                containerColor = colorResource(id = R.color.colorAccent),
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = SharedElements.Fab),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
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
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = SharedElements.NavButton),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
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
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
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
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 64.dp + imePadding
                    )
                ) {
                    items(items = games, key = { it.id }) { game ->
                        GameCard(
                            animatedVisibilityScope = animatedVisibilityScope,
                            game = game,
                            sortStat = sortStat,
                            showInfoControls = showInfoControls,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.GameCard(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    game: Game,
    sortStat: SortStat,
    showInfoControls: ShowInfoControls
) {
    var isLoadingImage by remember { mutableStateOf(true) }
    val checkmarkColor = if (game.timesCompleted > 0) {
        colorResource(R.color.success)
    } else {
        Color.LightGray
    }

    val showStat = if (sortStat == SortStat.NONE) {
        showInfoControls.getShowStat()
    } else {
        sortStat.getShowStat()
    }

    val hours = when (showStat) {
        ShowStat.HoursMain -> game.gameHoursStats.gameplayMain
        ShowStat.HoursMainExtra -> game.gameHoursStats.gameplayMainExtra
        ShowStat.HoursCompletionist -> game.gameHoursStats.gameplayCompletionist
        ShowStat.None -> 0.0
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
                    model = ImageRequest.Builder(LocalContext.current)
                        .crossfade(true)
                        .data(game.imageUri)
                        .placeholderMemoryCacheKey(SharedElements.GameImage(game.id).toString())
                        .memoryCacheKey(SharedElements.GameImage(game.id).toString())
                        .build(),
                    contentDescription = stringResource(id = R.string.game_cover),
                    contentScale = ContentScale.Fit,
                    onLoading = { isLoadingImage = true },
                    onSuccess = { isLoadingImage = false },
                    onError = { isLoadingImage = false },
                    error = painterResource(R.drawable.game_controller),
                    modifier = modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = SharedElements.GameImage(game.id)),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
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
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Is completed",
                        tint = checkmarkColor,
                        modifier = Modifier
                            .weight(15f)
                            .fillMaxSize()
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

            if (showStat != ShowStat.None) {
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

@OptIn(ExperimentalSharedTransitionApi::class)
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
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GamesFromPlatformScreen(
                    animatedVisibilityScope = this,
                    snackbarHostState = SnackbarHostState(),
                    games = games,
                    platformName = "Nintendo Switch",
                    sortStat = SortStat.HOURS_MAIN,
                    sortControls = SortControls(),
                    showInfoControls = ShowInfoControls(),
                    genresFilters = emptyList(),
                    searchQuery = "",
                    filteredStats = filterStats
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
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
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameCard(
                    game = game,
                    animatedVisibilityScope = this,
                    sortStat = SortStat.HOURS_MAIN,
                    showInfoControls = ShowInfoControls()
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(device = "spec:width=1080px,height=600px,dpi=440,orientation=portrait")
@Composable
private fun GameCardNoSortStatPreview() {
    val game = Game(name = "The Witcher 3: Wild Hunt")
    AppTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameCard(
                    animatedVisibilityScope = this,
                    game = game,
                    sortStat = SortStat.NONE,
                    showInfoControls = ShowInfoControls()
                )
            }
        }
    }
}