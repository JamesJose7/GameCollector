package com.jeeps.gamecollector.remaster.ui.games.details

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityGameDetailsBinding
import com.jeeps.gamecollector.databinding.ContentGameDetailsBinding
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.ADD_GAME_RESULT_MESSAGE
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM_NAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME_POSITION
import com.jeeps.gamecollector.remaster.ui.games.edit.AddGameActivity
import com.jeeps.gamecollector.remaster.utils.extensions.showSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.jeeps.gamecollector.utils.ColorsUtils.getColorByHoursRange
import com.jeeps.gamecollector.utils.FormatUtils
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
        window.enterTransition = Explode()
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
        bindHoursErrorMessage()
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
                val title = game.shortName.ifEmpty { game.name }
                content.gameTitle.text = title

                if (game.publisher.isEmpty())
                    content.gamePublisher.visibility = View.GONE
                else
                    content.gamePublisher.text = game.publisher
                content.gamePlatform.text = game.platform

                content.completeSwitch.isChecked = game.timesCompleted > 0
                setupCompleteSwitch()

                getCoverColors()
            }
        }

        viewModel.gameHoursStats.observe(this) {
            it?.let { stats ->
                formatGamePlayHours(stats)
            }
        }
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

    private fun bindHoursErrorMessage() {
        viewModel.showHoursErrorMessage.observe(this) {
            val showMessage = it ?: false
            content.hoursErrorMessage.visibility = if (showMessage) View.VISIBLE else View.GONE
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

    private fun formatGamePlayHours(stats: GameplayHoursStats) {
        content.storyHours.text =
            getString(R.string.hours_template, FormatUtils.formatDecimal(stats.gameplayMain))
        content.storyHours.setTextColor(getColorByHoursRange(this, stats.gameplayMain))

        content.mainExtraHours.text =
            getString(R.string.hours_template, FormatUtils.formatDecimal(stats.gameplayMainExtra))
        content.mainExtraHours.setTextColor(getColorByHoursRange(this, stats.gameplayMainExtra))

        content.completionistHours.text =
            getString(R.string.hours_template, FormatUtils.formatDecimal(stats.gameplayCompletionist))
        content.completionistHours.setTextColor(getColorByHoursRange(this, stats.gameplayCompletionist))
    }
}