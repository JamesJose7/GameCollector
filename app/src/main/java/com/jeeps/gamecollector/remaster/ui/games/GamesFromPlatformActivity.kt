package com.jeeps.gamecollector.remaster.ui.games

import android.content.DialogInterface
import android.os.Bundle
import android.transition.Explode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.adapters.GameCardAdapter
import com.jeeps.gamecollector.comparators.*
import com.jeeps.gamecollector.databinding.ActivityPlatformLibraryBinding
import com.jeeps.gamecollector.databinding.ContentPlatformLibraryBinding
import com.jeeps.gamecollector.model.SortStat
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.createSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.dpToPx
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.jeeps.gamecollector.utils.PlatformCovers
import com.jeeps.gamecollector.views.GridSpacingItemDecoration
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GamesFromPlatformActivity : BaseActivity(),
    GameCardAdapter.GameCardAdapterListener {

    private val binding by viewBinding(ActivityPlatformLibraryBinding::inflate)
    private lateinit var content: ContentPlatformLibraryBinding

    private val viewModel: GamesFromPlatformViewModel by viewModels()

    private lateinit var gamesAdapter: GameCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.exitTransition = Explode()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        content = binding.content

        displayPlatformCover()

        initCollapsingToolbar()
        initializeGamesAdapter()

        bindFab()
        getIntentData()

        bindUserGames()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.deleteGamePendingDeletion()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_platform_library, menu)

        val searchView: SearchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.handleSearch(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.handleSearch(it) }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        gamesAdapter.setSortStat(SortStat.NONE)

        return when (item.itemId) {
            R.id.action_filter_alph -> {
                viewModel.rearrangeGames(GameByNameComparator())
                true
            }
            R.id.action_filter_alph_desc -> {
                viewModel.rearrangeGames(GameByNameComparator(true))
                true
            }
            R.id.action_filter_physical -> {
                viewModel.rearrangeGames(GameByPhysicalComparator(true))
                true
            }
            R.id.action_filter_alph_physical_desc -> {
                viewModel.rearrangeGames(GameByPhysicalComparator())
                true
            }
            R.id.action_filter_timesc -> {
                viewModel.rearrangeGames(GameByTimesPlayedComparator())
                true
            }
            R.id.action_filter_alph_timesc_desc -> {
                viewModel.rearrangeGames(GameByTimesPlayedComparator(true))
                true
            }
            R.id.action_filter_hoursmain -> {
                viewModel.rearrangeGames(GameByHoursStoryComparator())
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN)
                true
            }
            R.id.action_filter_hoursmain_desc -> {
                viewModel.rearrangeGames(GameByHoursStoryComparator(true))
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN)
                true
            }
            R.id.action_filter_hoursme -> {
                viewModel.rearrangeGames(GameByHoursMainExtraComparator())
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN_EXTRA)
                true
            }
            R.id.action_filter_hoursme_desc -> {
                viewModel.rearrangeGames(GameByHoursMainExtraComparator(true))
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN_EXTRA)
                true
            }
            R.id.action_filter_hourscompletionist -> {
                viewModel.rearrangeGames(GameByHoursCompletionistComparator())
                gamesAdapter.setSortStat(SortStat.HOURS_COMPLETIONIST)
                true
            }
            R.id.action_filter_hourscompletionist_desc -> {
                viewModel.rearrangeGames(GameByHoursCompletionistComparator(true))
                gamesAdapter.setSortStat(SortStat.HOURS_COMPLETIONIST)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getIntentData() {
        intent.getStringExtra(CURRENT_PLATFORM)?.let { viewModel.platformId = it }
        intent.getStringExtra(CURRENT_PLATFORM_NAME)?.let { viewModel.platformName = it }
    }

    private fun displayPlatformCover() {
        //TODO: Should display the user's platform cover instead of default image
        Picasso.get()
            .load(PlatformCovers.getPlatformCover(viewModel.platformName))
            .into(binding.backdrop)
    }

    private fun initCollapsingToolbar() {
        val collapsingToolbar = binding.collapsingToolbar
        val appBar = binding.appbar

        collapsingToolbar.title = " "
        appBar.setExpanded(true)

        // hiding & showing the title when toolbar expanded & collapsed
        appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShowing = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout?.totalScrollRange ?: -1
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.title = viewModel.platformName
                    isShowing = true
                } else if (isShowing) {
                    collapsingToolbar.title = " "
                    isShowing = false
                }
            }

        })
    }

    private fun initializeGamesAdapter() {
        val gamesRecyclerView = content.gamesRecyclerView
        gamesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        gamesRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(2, dpToPx(10f), true))
        gamesRecyclerView.itemAnimator = DefaultItemAnimator()

        gamesAdapter = GameCardAdapter(this, mutableListOf(), this)
        gamesRecyclerView.adapter = gamesAdapter
    }

    private fun bindFab() {
        // TODO: Bind fab to add game
        binding.fab.setOnClickListener {

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

        viewModel.errorMessage.observe(this) {
            it?.let { showToast(it) }
        }

        viewModel.serverMessage.observe(this) { messageEvent ->
            messageEvent.getContentIfNotHandled()?.let {
                showToast(it)
            }
        }
    }

    override fun deleteSelectedGame(position: Int) {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val selectedGame = viewModel.getGameAt(position)
                    viewModel.gamePendingDeletion = selectedGame
                    // Remove game from adapter
                    gamesAdapter.removeGameAtPosition(position)
                    // Notify user
                    val undoCallback = object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            // Delete game permanently
                            selectedGame?.let { viewModel.deleteGame(it) }
                        }
                    }
                    val undoSnackBar = createSnackBar(
                        binding.root,
                        "Deleted: ${selectedGame?.name}",
                        Snackbar.LENGTH_LONG
                    ).addCallback(undoCallback)

                    undoSnackBar.setAction("UNDO") {
                        undoSnackBar.removeCallback(undoCallback)
                        // Restore game
                        gamesAdapter.addGameAtPosition(position, selectedGame)
                    }

                    undoSnackBar.show()
                }
            }
        }

        AlertDialog.Builder(this)
            .setMessage("Delete game?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener)
            .show()
    }

    override fun editGame(position: Int, imageView: View?, titleView: TextView?) {
        TODO("Not yet implemented")
    }

    companion object {
        const val CURRENT_PLATFORM = "CURRENT_PLATFORM"
        const val CURRENT_PLATFORM_NAME = "CURRENT_PLATFORM_NAME"
    }
}