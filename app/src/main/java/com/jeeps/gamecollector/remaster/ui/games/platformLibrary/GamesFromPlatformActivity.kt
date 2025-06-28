package com.jeeps.gamecollector.remaster.ui.games.platformLibrary

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityPlatformLibraryBinding
import com.jeeps.gamecollector.databinding.ContentPlatformLibraryBinding
import com.jeeps.gamecollector.deprecated.adapters.GameCardAdapter
import com.jeeps.gamecollector.deprecated.utils.ColorsUtils
import com.jeeps.gamecollector.deprecated.utils.PlatformCovers
import com.jeeps.gamecollector.deprecated.views.GridSpacingItemDecoration
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.composables.Dialog
import com.jeeps.gamecollector.remaster.ui.composables.LoadingAnimation
import com.jeeps.gamecollector.remaster.ui.games.details.GameDetailsActivity
import com.jeeps.gamecollector.remaster.ui.games.edit.AddGameActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.AdvancedFiltersDialog
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.AdvancedFiltersDialogListener
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.FilterControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.ShowInfoControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.SortControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getAppropriateComparator
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getFilterData
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getInfoControlsFromSortStat
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getInfoData
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import com.jeeps.gamecollector.remaster.utils.comparators.GameByHoursCompletionistComparator
import com.jeeps.gamecollector.remaster.utils.comparators.GameByHoursMainExtraComparator
import com.jeeps.gamecollector.remaster.utils.comparators.GameByHoursStoryComparator
import com.jeeps.gamecollector.remaster.utils.comparators.GameByNameComparator
import com.jeeps.gamecollector.remaster.utils.comparators.GameByPhysicalComparator
import com.jeeps.gamecollector.remaster.utils.comparators.GameByTimesPlayedComparator
import com.jeeps.gamecollector.remaster.utils.extensions.dpToPx
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.showSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
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
class GamesFromPlatformActivity : BaseActivity(),
    GameCardAdapter.GameCardAdapterListener, AdvancedFiltersDialogListener {

    private val binding by viewBinding(ActivityPlatformLibraryBinding::inflate)
    private lateinit var content: ContentPlatformLibraryBinding

    private lateinit var searchView: SearchView

    private val viewModel: GamesFromPlatformViewModel by viewModels()

    private var isAnimating: Boolean = false
    private var currentFilterAnimationState = FilterHeaderAnimationState.STATE_DOWN

    private val addGameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.let { handleAddGameResult(it) }
        }

    private lateinit var gamesAdapter: GameCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.exitTransition = Fade().withExclusions()
        window.enterTransition = Fade().withExclusions()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        content = binding.content

        initCollapsingToolbar()
        initializeGamesAdapter()
        addFilterStatsHeaderAnimation()

        bindFab()
        getIntentData()
        displayPlatformCover()

        bindUserGames()
        bindFilterStats()

        binding.screenCompose.setComposable {
            GamesFromPlatformScreen(
                viewModel = viewModel,
                onBackPressed = {
                    onBackPressedDispatcher.onBackPressed()
                },
                onAdvancedFiltersClicked = {
                    // TODO: Replace with compose dialog
                    val advancedFiltersDialog = AdvancedFiltersDialog(
                        this,
                        this,
                        viewModel.currentFilterControls.value ?: FilterControls(),
                        viewModel.currentSortControls,
                        getInfoControlsFromSortStat(
                            viewModel.currentSortStat.value
                        )
                    )
                    advancedFiltersDialog.show()
                },
                onEditGame = { game ->

                },
            )
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.deleteGamePendingDeletion()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_platform_library, menu)

        searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.handleSearch(it)
                    viewModel.clearFilters()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    viewModel.handleSearch(it)
                    viewModel.clearFilters()
                }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_advanced_filters -> {
                val advancedFiltersDialog = AdvancedFiltersDialog(
                    this,
                    this,
                    viewModel.currentFilterControls.value ?: FilterControls(),
                    viewModel.currentSortControls,
                    getInfoControlsFromSortStat(
                        viewModel.currentSortStat.value
                    )
                )
                advancedFiltersDialog.show()
                true
            }
            R.id.action_filter_alph -> {
                viewModel.setCurrentSortStat(SortStat.NONE)
                viewModel.rearrangeGames(GameByNameComparator())
                true
            }
            R.id.action_filter_alph_desc -> {
                viewModel.setCurrentSortStat(SortStat.NONE)
                viewModel.rearrangeGames(GameByNameComparator(true))
                true
            }
            R.id.action_filter_physical -> {
                viewModel.setCurrentSortStat(SortStat.NONE)
                viewModel.rearrangeGames(GameByPhysicalComparator(true))
                true
            }
            R.id.action_filter_alph_physical_desc -> {
                viewModel.setCurrentSortStat(SortStat.NONE)
                viewModel.rearrangeGames(GameByPhysicalComparator())
                true
            }
            R.id.action_filter_timesc -> {
                viewModel.setCurrentSortStat(SortStat.NONE)
                viewModel.rearrangeGames(GameByTimesPlayedComparator())
                true
            }
            R.id.action_filter_alph_timesc_desc -> {
                viewModel.setCurrentSortStat(SortStat.NONE)
                viewModel.rearrangeGames(GameByTimesPlayedComparator(true))
                true
            }
            R.id.action_filter_hoursmain -> {
                viewModel.setCurrentSortStat(SortStat.HOURS_MAIN)
                viewModel.rearrangeGames(GameByHoursStoryComparator())
                true
            }
            R.id.action_filter_hoursmain_desc -> {
                viewModel.setCurrentSortStat(SortStat.HOURS_MAIN)
                viewModel.rearrangeGames(GameByHoursStoryComparator(true))
                true
            }
            R.id.action_filter_hoursme -> {
                viewModel.setCurrentSortStat(SortStat.HOURS_MAIN_EXTRA)
                viewModel.rearrangeGames(GameByHoursMainExtraComparator())
                true
            }
            R.id.action_filter_hoursme_desc -> {
                viewModel.setCurrentSortStat(SortStat.HOURS_MAIN_EXTRA)
                viewModel.rearrangeGames(GameByHoursMainExtraComparator(true))
                true
            }
            R.id.action_filter_hourscompletionist -> {
                viewModel.setCurrentSortStat(SortStat.HOURS_COMPLETIONIST)
                viewModel.rearrangeGames(GameByHoursCompletionistComparator())
                true
            }
            R.id.action_filter_hourscompletionist_desc -> {
                viewModel.setCurrentSortStat(SortStat.HOURS_COMPLETIONIST)
                viewModel.rearrangeGames(GameByHoursCompletionistComparator(true))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun resetSearchView() {
//        searchView.setQuery("", false)
    }

    override fun updateFilterControls(filterControls: FilterControls) {
        resetSearchView()
        val (filtersList) = filterControls.getFilterData()
        viewModel.setFilterControls(filterControls)
        viewModel.updateFilters(filtersList)
    }

    override fun clearFilters() {
        viewModel.clearFilters(true)
        resetSearchView()
    }

    override fun updateSortControls(sortControls: SortControls) {
        val (comparator, sortStat) = sortControls.getAppropriateComparator()
        viewModel.currentSortControls = sortControls
        viewModel.setCurrentSortStat(sortStat)
        viewModel.rearrangeGames(comparator)
    }

    override fun updateShowInfoControls(showInfoControls: ShowInfoControls) {
        val (sortStat) = showInfoControls.getInfoData()
        viewModel.currentShowInfoControls = showInfoControls
        viewModel.setCurrentSortStat(sortStat)
        gamesAdapter.notifyItemRangeChanged(0, viewModel.games.value?.size ?: 0)
    }

    private fun getIntentData() {
        intent.getStringExtra(CURRENT_PLATFORM)?.let { viewModel.platformId = it }
        intent.getStringExtra(CURRENT_PLATFORM_NAME)?.let { viewModel.platformName = it }
    }

    private fun displayPlatformCover() {
        //TODO: Should display the user's platform cover instead of default image
//        Picasso.get()
//            .load(PlatformCovers.getPlatformCover(viewModel.platformName))
//            .into(binding.backdrop)
    }

    private fun initCollapsingToolbar() {
//        val collapsingToolbar = binding.collapsingToolbar
//        val appBar = binding.appbar
//
//        collapsingToolbar.title = " "
//        appBar.setExpanded(true)

        // hiding & showing the title when toolbar expanded & collapsed
//        appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
//            var isShowing = false
//            var scrollRange = -1
//
//            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
//                if (scrollRange == -1) {
//                    scrollRange = appBarLayout?.totalScrollRange ?: -1
//                }
//                if (scrollRange + verticalOffset == 0) {
//                    collapsingToolbar.title = viewModel.platformName
//                    isShowing = true
//                } else if (isShowing) {
//                    collapsingToolbar.title = " "
//                    isShowing = false
//                }
//            }
//
//        })
    }

    private fun initializeGamesAdapter() {
        val gamesRecyclerView = content.gamesRecyclerView
        gamesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        gamesRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(2, dpToPx(10f), true))
        gamesRecyclerView.itemAnimator = DefaultItemAnimator()

        gamesAdapter = GameCardAdapter(mutableListOf(), this)
        gamesRecyclerView.adapter = gamesAdapter
    }

    private fun bindFab() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddGameActivity::class.java).apply {
                putExtra(CURRENT_PLATFORM, viewModel.platformId)
                putExtra(CURRENT_PLATFORM_NAME, viewModel.platformName)
            }
            addGameResultLauncher.launch(intent)
        }
    }

    private fun bindUserGames() {
        viewModel.isLoading.observe(this) {
            it?.let { isLoading ->
                content.gamesProgressbar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            }
        }

        viewModel.games.observe(this) { games ->
            games?.let {
                gamesAdapter.setGames(it)
            }
        }

        viewModel.currentSortStat.observe(this) {
            it?.let { gamesAdapter.setSortStat(it) }
        }

        viewModel.errorMessage.observe(this) {
            it?.let { showToast(it) }
        }

        viewModel.serverMessage.observe(this) { messageEvent ->
            messageEvent.getContentIfNotHandled()?.let {
                showToast(it)
            }
        }
    }

    private fun bindFilterStats() {
        viewModel.filteredStats.observe(this) {
            it?.let { stats ->
                content.filterStatsCard.visibility = if (stats.showStats) View.VISIBLE else View.GONE
                val plural = if (stats.totalAmount <= 1) "" else "s"
                val statsText = "Showing ${stats.filteredAmount} out of ${stats.totalAmount} game$plural"
                content.filterStatsTv.text = statsText
            }
        }
    }

    override fun editGame(position: Int, imageView: View, titleView: TextView) {
        val selectedGame = viewModel.getGameAt(position)
        val intent = Intent(this, GameDetailsActivity::class.java).apply {
            putExtra(CURRENT_PLATFORM, viewModel.platformId)
            putExtra(CURRENT_PLATFORM_NAME, viewModel.platformName)
            putExtra(SELECTED_GAME, selectedGame)
            putExtra(SELECTED_GAME_POSITION, position)
        }
        val activityOptions = ActivityOptionsCompat
            .makeSceneTransitionAnimation(
                this,
                Pair.create(imageView, "cover"),
                Pair.create(binding.fab, "fab")
            )
        addGameResultLauncher.launch(intent, activityOptions)
    }

    private fun handleAddGameResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra(ADD_GAME_RESULT_MESSAGE)?.let { message ->
                showSnackBar(binding.root, message)
            }
        }
    }

    private fun addFilterStatsHeaderAnimation() {
        content.gamesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1) &&
                        recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    runFilterHeaderAnimation(FilterHeaderAnimationState.STATE_DOWN)
                } else if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    runFilterHeaderAnimation(FilterHeaderAnimationState.STATE_TOP)
                }
            }
        })
    }

    private fun runFilterHeaderAnimation(animationState: FilterHeaderAnimationState) {
        if (!isAnimating && animationState != currentFilterAnimationState) {
            val newMargin = if (animationState == FilterHeaderAnimationState.STATE_TOP) {
                dpToPx(12f)
             } else {
                dpToPx(55f)
            }
            val layoutParams: ConstraintLayout.LayoutParams? =
                content.filterStatsCard.layoutParams as? ConstraintLayout.LayoutParams
            val animator: ValueAnimator = ValueAnimator.ofInt(layoutParams?.topMargin ?: 0, newMargin)
            animator.addUpdateListener { valueAnimator ->
                layoutParams?.topMargin = valueAnimator.animatedValue as? Int ?: 0
                content.filterStatsCard.requestLayout()
            }

            animator.addListener(
                onStart = { isAnimating = true },
                onEnd = {
                    isAnimating = false
                    currentFilterAnimationState = animationState
                }
            )
            animator.duration = 300
            animator.start()
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

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun GamesFromPlatformScreen(
    viewModel: GamesFromPlatformViewModel = viewModel(),
    onBackPressed: () -> Unit,
    onAdvancedFiltersClicked: () -> Unit,
    onEditGame: (Game) -> Unit
) {
    val games by viewModel.games.observeAsState(emptyList())
    val sortStat by viewModel.currentSortStat.observeAsState(SortStat.NONE)
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showDeleteGameDialog by remember { mutableStateOf(false) }
    var gamePendingDeletion: Game? by remember { mutableStateOf(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        onBackPressed = onBackPressed,
        onAdvancedFiltersClicked = onAdvancedFiltersClicked,
        onSearchQueryChanged = {
            viewModel.handleSearch(it)
        },
        onEditGame = onEditGame,
        onDeleteGame = {
            gamePendingDeletion = it
            showDeleteGameDialog = true
        }
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
    onBackPressed: () -> Unit = {},
    onAdvancedFiltersClicked: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onEditGame: (Game) -> Unit = {},
    onDeleteGame: (Game) -> Unit = {}
) {
    val collapsingScaffoldState = rememberCollapsingToolbarScaffoldState()
    val platformCover = PlatformCovers.getPlatformCover(platformName)

    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {  },
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 64.dp),
                modifier = modifier
                    .padding(innerPadding)
            ) {
                items(games) { game ->
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

@Preview
@Composable
private fun GamesFromPlatformScreenPreview() {
    val games = List(10) {
        Game(
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

    AppTheme {
        GamesFromPlatformScreen(
            snackbarHostState = SnackbarHostState(),
            games = games,
            platformName = "Nintendo Switch",
            sortStat = SortStat.HOURS_MAIN,
            searchQuery = ""
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