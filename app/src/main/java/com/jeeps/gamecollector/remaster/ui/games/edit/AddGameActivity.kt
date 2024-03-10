package com.jeeps.gamecollector.remaster.ui.games.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityAddGameBinding
import com.jeeps.gamecollector.databinding.ContentAddGameBinding
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.ADD_GAME_RESULT_MESSAGE
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM_NAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME_POSITION
import com.jeeps.gamecollector.remaster.utils.extensions.compressImage
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.roundToInt
import android.graphics.Color as GraphicsColor

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
        bindAlerts()
        bindLoading()
        bindFab()
        bindFormFields()

        bindImageUploadEvent()
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
        content.gameCover.setBackgroundColor(GraphicsColor.parseColor("#99cccccc"))
    }

    private fun removeImageCover() {
        viewModel.coverDeleted = true
        viewModel.setGameImageUri(null)
        content.gameCover.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_image))
        content.gameCover.setBackgroundColor(GraphicsColor.parseColor("#cccccc"))
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
                content.gameCover.setBackgroundColor(GraphicsColor.parseColor("#99cccccc"))
            } else {
                viewModel.coverDeleted = true
                Picasso.get().load(R.drawable.edit_picture).into(content.gameCover)
                content.gameCover.setBackgroundColor(GraphicsColor.parseColor("#cccccc"))
            }
            // Names
            content.gameNameEdit.setText(game.name)
            content.gameShortnameEdit.setText(game.shortName)
            // Set physical or digital
            if (!game.isPhysical) {
                content.radioGroup.check(content.radioDigital.id)
            }
            // Times completed
            game.timesCompleted.let {
                content.timesCompletedSelector.value = it
                viewModel.setTimesCompleted(it)
            }
            // Publisher
            content.gamePublisherEdit.setText(game.publisher)
        }
    }

    private fun bindFab() {
        binding.fab.setOnClickListener {
            viewModel.saveGame()
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

    private fun bindAlerts() {
        viewModel.errorMessage.observe(this) {
            it?.let {
                showToast(it, Toast.LENGTH_LONG)
            }
        }

        viewModel.serverMessage.observe(this) { messageEvent ->
            messageEvent?.getContentIfNotHandled()?.let {
                finishActivityWithResult(it)
            }
        }
    }

    private fun finishActivityWithResult(message: String) {
        val intent = Intent().apply {
            putExtra(ADD_GAME_RESULT_MESSAGE, message)
        }
        setResult(RESULT_OK, intent)
        finish()
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

    private fun bindImageUploadEvent() {
        viewModel.isImageReadyToUpload.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { isReadyToUpload ->
                if (isReadyToUpload) {
                    viewModel.uploadCoverImage(
                        viewModel.currentImageUri?.let { compressImage("temp.png", it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddGameScreen(
    modifier: Modifier = Modifier,
    name: String,
    shortName: String,
    platform: String,
    publisher: String,
    isPhysical: Boolean,
    timesCompleted: Int,
    coverImageUri: Uri? = null,
    onNameChange: (String) -> Unit = { },
    onShortNameChange: (String) -> Unit = { },
    onPlatformChange: (String) -> Unit = { },
    onPublisherChange: (String) -> Unit = { },
    onIsPhysicalChange: (Boolean) -> Unit = { },
    onTimesCompletedChange: (Int) -> Unit = { },
    onCoverImageChange: (Uri?) -> Unit = { },
) {
    var selectedCover by rememberSaveable { mutableStateOf(coverImageUri) }
    val pickMedia = rememberLauncherForActivityResult(contract = PickVisualMedia()) { uri ->
        selectedCover = uri
        onCoverImageChange(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedCover)
                    .placeholder(R.drawable.ic_add_image)
                    .fallback(R.drawable.ic_add_image)
                    .build(),
                contentScale = ContentScale.Fit,
                contentDescription = "Game cover",
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(color = Color(0xFFCCCCCC))
                    .clickable {
                        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }
            )
            IconButton(
                onClick = {
                    selectedCover = null
                    onCoverImageChange(null)
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = stringResource(id = R.string.cover_disclaimer),
            color = colorResource(id = R.color.textSecondaryColor),
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(all = 10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(text = "Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = shortName,
                onValueChange = onShortNameChange,
                label = { Text(text = "Short Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedTextField(
                    value = platform,
                    onValueChange = onPlatformChange,
                    label = { Text(text = "Platform") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedTextField(
                    value = publisher,
                    onValueChange = onPublisherChange,
                    label = { Text(text = "Publisher") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(text = "Digital", modifier = Modifier.clickable { onIsPhysicalChange(false) })
                Switch(
                    checked = isPhysical,
                    onCheckedChange = onIsPhysicalChange,
                    thumbContent = if (isPhysical) {
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_physical),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_digital),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedBorderColor = MaterialTheme.colorScheme.secondary,
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(text = "Physical", modifier = Modifier.clickable { onIsPhysicalChange(true) })
            }

            Column {
                Slider(
                    value = timesCompleted.toFloat(),
                    onValueChange = { onTimesCompletedChange(it.roundToInt()) },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    steps = 5,
                    valueRange = 0f..5f
                )
                Text(text = "Times completed: $timesCompleted")
            }
        }
    }
}

@Preview
@Composable
fun AddGameScreenPreview() {
    var isPhysical by remember { mutableStateOf(true) }
    var timesCompleted by remember { mutableIntStateOf(0) }
    AppTheme {
        AddGameScreen(
            name = "",
            shortName = "",
            platform = "",
            publisher = "",
            isPhysical = isPhysical,
            timesCompleted = timesCompleted,
            onIsPhysicalChange = { isPhysical = it },
            onTimesCompletedChange = { timesCompleted = it}
        )
    }
}