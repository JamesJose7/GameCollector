package com.jeeps.gamecollector.remaster.ui.gamePlatorms

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.adapters.PlatformsListAdapter
import com.jeeps.gamecollector.databinding.ActivityMainLibraryBinding
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.value
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GamePlatformsActivity : BaseActivity() {
    private lateinit var binding: ActivityMainLibraryBinding

    private val viewModel: GamePlatformsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLibraryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Platforms"

        bindLoading()
        bindError()
        bindPlatforms()
        bindFab()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_library, menu)
        return true;
    }

    private fun bindLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading.value()) {
                binding.content.platformsProgressBar.visibility = View.VISIBLE
            } else {
                binding.content.platformsProgressBar.visibility = View.GONE
            }
        }
    }

    private fun bindError() {
        viewModel.errorMessage.observe(this) { error ->
            Log.e(TAG, error)
            Toast.makeText(this, "An error has occurred, please try again", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun bindPlatforms() {
        viewModel.platforms.observe(this) { platforms ->
            platforms?.let {
                initializePlatformsAdapter(it)
            }
        }
    }

    private fun bindFab() {
        binding.fab.setOnClickListener {
            // TODO: Implement new activity
        }
    }

    private fun initializePlatformsAdapter(platforms: List<Platform>) {
        val platformsList = binding.content.platformsList
        platformsList.layoutManager = LinearLayoutManager(this)
        platformsList.itemAnimator = DefaultItemAnimator()

        val platformsListAdapter = PlatformsListAdapter(this, this, platforms)
        platformsList.adapter = platformsListAdapter
    }

    companion object {
        const val RC_SIGN_IN = 420
        const val ADD_PLATFORM_RESULT = 13
        const val EDIT_PLATFORM_RESULT = 97
    }

}