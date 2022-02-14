package com.jeeps.gamecollector.remaster.ui.games

import android.os.Bundle
import android.transition.Explode
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.jeeps.gamecollector.adapters.GameCardAdapter
import com.jeeps.gamecollector.databinding.ActivityPlatformLibraryBinding
import com.jeeps.gamecollector.databinding.ContentPlatformLibraryBinding
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.dpToPx
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.jeeps.gamecollector.utils.PlatformCovers
import com.jeeps.gamecollector.views.GridSpacingItemDecoration
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

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

        getIntentData()
        displayPlatformCover()

        initCollapsingToolbar()
        initializeGamesAdapter()
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

    override fun deleteSelectedGame(position: Int) {
        TODO("Not yet implemented")
    }

    override fun editGame(position: Int, imageView: View?, titleView: TextView?) {
        TODO("Not yet implemented")
    }

    companion object {
        const val CURRENT_PLATFORM = "CURRENT_PLATFORM"
        const val CURRENT_PLATFORM_NAME = "CURRENT_PLATFORM_NAME"
    }
}