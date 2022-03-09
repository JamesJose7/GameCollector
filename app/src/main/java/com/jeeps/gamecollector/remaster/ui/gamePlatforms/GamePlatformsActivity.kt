package com.jeeps.gamecollector.remaster.ui.gamePlatforms

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.adapters.PlatformsListAdapter
import com.jeeps.gamecollector.databinding.ActivityMainLibraryBinding
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.login.LoginActivity
import com.jeeps.gamecollector.remaster.ui.userStats.UserStatsActivity
import com.jeeps.gamecollector.remaster.utils.extensions.showSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.value
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GamePlatformsActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMainLibraryBinding::inflate)

    private val viewModel: GamePlatformsViewModel by viewModels()

    private val addPlatformResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it?.let { handleAddPlatformResult(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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
            showToast("An error has occurred, please try again")
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
            intent = Intent(this, AddPlatformActivity::class.java)
            addPlatformResultLauncher.launch(intent)
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
                showToast("Something went wrong", Toast.LENGTH_LONG)
            }
    }

    private fun handleAddPlatformResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            showSnackBar(binding.root, "Successfully added platform")
        }
    }

}