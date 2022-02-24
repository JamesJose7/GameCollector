package com.jeeps.gamecollector.remaster.ui.games.details

import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import com.jeeps.gamecollector.databinding.ActivityGameDetailsBinding
import com.jeeps.gamecollector.databinding.ContentGameDetailsBinding
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameDetailsActivity : BaseActivity() {

    private val binding by viewBinding(ActivityGameDetailsBinding::inflate)
    private lateinit var content: ContentGameDetailsBinding

    private val viewModel: GameDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup exit transition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.enterTransition = Explode()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        super.onCreate(savedInstanceState)
        setContentView(binding.rootLayout)
        setSupportActionBar(binding.toolbar)
        content = binding.content
    }
}