package com.jeeps.gamecollector.remaster.ui.games.edit

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityAddGameBinding
import com.jeeps.gamecollector.databinding.ContentAddGameBinding
import com.jeeps.gamecollector.model.Game
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.games.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM
import com.jeeps.gamecollector.remaster.ui.games.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM_NAME
import com.jeeps.gamecollector.remaster.ui.games.GamesFromPlatformActivity.Companion.SELECTED_GAME
import com.jeeps.gamecollector.remaster.ui.games.GamesFromPlatformActivity.Companion.SELECTED_GAME_POSITION
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddGameActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAddGameBinding::inflate)
    private lateinit var content: ContentAddGameBinding

    private val viewModel: AddGameViewModel by viewModels()

    private val registerForOpenDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri -> handleImageSelected(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        content = binding.content
        supportActionBar?.title = "Add New Game"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getIntentData()
        populateFormDefaults()

        checkIfGameIsBeingEdited()
        bindLoading()
        bindFab()
        bindFormFields()
    }

    private fun getIntentData() {
        viewModel.platformId = intent.getStringExtra(CURRENT_PLATFORM)
        viewModel.platformName = intent.getStringExtra(CURRENT_PLATFORM_NAME)
        (intent.getSerializableExtra(SELECTED_GAME) as? Game)?.let { viewModel.setSelectedGame(it) }
        viewModel.selectedGamePosition = intent.getIntExtra(SELECTED_GAME_POSITION, -1)
    }

    private fun populateFormDefaults() {
        content.radioGroup.check(content.radioPhysical.id)

        content.timesCompletedSelector.minValue = 0
        content.timesCompletedSelector.maxValue = 10

        content.platformGameEdit.setText(viewModel.platformName ?: "")

        content.gameCover.setOnClickListener {
            // Invalidate picasso cache
            viewModel.selectedGame.value?.imageUri?.let { uri ->
                Picasso.get().invalidate(uri)
            }
            registerForOpenDocument.launch(arrayOf("image/*"))
        }
    }

    private fun handleImageSelected(uri: Uri) {
        viewModel.coverDeleted = false
        viewModel.setGameImageUri(uri)
        Picasso.get().load(uri).into(content.gameCover)
        content.gameCover.setBackgroundColor(Color.parseColor("#99cccccc"))
    }

    private fun removeImageCover() {
        viewModel.coverDeleted = true
        viewModel.setGameImageUri(null)
        Picasso.get().load(R.drawable.ic_add_image).into(content.gameCover)
        content.gameCover.setBackgroundColor(Color.parseColor("#cccccc"))
    }

    private fun checkIfGameIsBeingEdited() {
        if (viewModel.selectedGame.value != null) {
            supportActionBar?.title = "Edit Game"
            binding.fab.setImageResource(R.drawable.edit)
            mapSelectedGameFields()
        } else {
            viewModel.initializeDefaultGame()
        }
    }

    private fun mapSelectedGameFields() {
        viewModel.selectedGame.value?.let { game ->
            // Images
            if (game.imageUri.isNotEmpty()) {
                Picasso.get().load(game.imageUri).into(content.gameCover)
                content.gameCover.setBackgroundColor(Color.parseColor("#99cccccc"))
            } else {
                viewModel.coverDeleted = true
                Picasso.get().load(R.drawable.edit_picture).into(content.gameCover)
                content.gameCover.setBackgroundColor(Color.parseColor("#cccccc"))
            }
            // Names
            content.gameNameEdit.setText(game.name)
            content.gameShortnameEdit.setText(game.shortName)
            // Set physical or digital
            if (!game.isPhysical()) {
                content.radioGroup.check(content.radioDigital.id)
            }
            // Times completed
            game.timesCompleted.let {
                content.timesCompletedSelector.value = it
                viewModel.setTimesCompleted(it)
            }
            // Publisher
            game.publisher?.let {
                content.gamePublisherEdit.setText(it)
            }
        }
    }

    private fun bindFab() {
        binding.fab.setOnClickListener {
            saveGame()
        }
    }

    private fun bindLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            toggleProgressbar(isLoading)
        }
    }

    private fun toggleProgressbar(isLoading: Boolean) {
        if (isLoading) {
            binding.addGameProgressbar.visibility = View.VISIBLE
            binding.fab.visibility = View.INVISIBLE
        } else {
            binding.addGameProgressbar.visibility = View.INVISIBLE
            binding.fab.visibility = View.VISIBLE
        }
    }

    private fun bindFormFields() {
        content.gameNameEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.setGameName(text.toString())
        }

        content.gameShortnameEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.setGameShortName(text.toString())
        }

        content.gamePublisherEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.setGamePublisher(text.toString())
        }

        content.timesCompletedSelector.setOnValueChangedListener { _, _, newVal ->
            viewModel.setTimesCompleted(newVal)
        }

        content.removeCoverButton.setOnClickListener {
            removeImageCover()
        }
    }

    fun onGameFormatClicked(view: View) {
        if (view !is RadioButton) return

        when (view.id) {
            R.id.radio_digital ->
                viewModel.setGameFormat(false)
            R.id.radio_physical ->
                viewModel.setGameFormat(true)
        }
    }

    private fun saveGame() {
        // TODO: Add save game feature
    }
}