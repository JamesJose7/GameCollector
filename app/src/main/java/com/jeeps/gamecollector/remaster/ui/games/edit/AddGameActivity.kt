package com.jeeps.gamecollector.remaster.ui.games.edit

import android.os.Bundle
import androidx.activity.viewModels
import com.jeeps.gamecollector.databinding.ActivityAddGameBinding
import com.jeeps.gamecollector.databinding.ContentAddGameBinding
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddGameActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAddGameBinding::inflate)
    private lateinit var content: ContentAddGameBinding

    private val viewModel: AddGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        content = binding.content
    }
}