package com.jeeps.gamecollector.remaster.ui.gamePlatorms

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.adapters.PlatformsListAdapter
import com.jeeps.gamecollector.databinding.ActivityMainLibraryBinding
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.login.LoginActivity
import com.jeeps.gamecollector.remaster.ui.userStats.UserStatsActivity
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

        checkIfUserIsLoggedIn()

        bindLoading()
        bindError()
        bindPlatforms()
        bindFab()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_library, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_stats -> {
                val intent = Intent(this, UserStatsActivity::class.java)
                startActivity(intent)
            }
            R.id.action_logout -> logout()
        }

        return super.onOptionsItemSelected(item)
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

    private fun checkIfUserIsLoggedIn() {
        viewModel.isUserLoggedIn.observe(this) { isLoggedIn ->
            if (!isLoggedIn.value()) {
                promptUserLogin()
            }
        }
    }

    private fun promptUserLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                promptUserLogin()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wront", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        const val ADD_PLATFORM_RESULT = 13
        const val EDIT_PLATFORM_RESULT = 97
    }

}