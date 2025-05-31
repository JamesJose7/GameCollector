@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)

package com.jeeps.gamecollector.remaster.ui.games.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityAddGameBinding
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.ADD_GAME_RESULT_MESSAGE
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.CURRENT_PLATFORM_NAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity.Companion.SELECTED_GAME_POSITION
import com.jeeps.gamecollector.remaster.utils.extensions.serializable
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddGameActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAddGameBinding::inflate)

    private val viewModel: AddGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getIntentData()

        bindAlerts()

        binding.screenCompose.setComposable {
            AddGameScreen(viewModel) { onBackPressedDispatcher.onBackPressed() }
        }
    }

    private fun getIntentData() {
        viewModel.platformId = intent.getStringExtra(CURRENT_PLATFORM)
        viewModel.platformName = intent.getStringExtra(CURRENT_PLATFORM_NAME)
        intent.serializable<Game>(SELECTED_GAME)?.let { viewModel.setSelectedGame(it) }
        viewModel.selectedGamePosition = intent.getIntExtra(SELECTED_GAME_POSITION, -1)
        viewModel.checkIfGameIsBeingEdited()
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
}

@Composable
fun AddGameScreen(
    addGameViewModel: AddGameViewModel = viewModel(),
    onBackPressed: () -> Unit = {}
) {
    val game by addGameViewModel.selectedGame.collectAsState()
    val isLoading by addGameViewModel.isLoading.observeAsState(false)

    AddGameScreen(
        name = game.name,
        shortName = game.shortName,
        platform = game.platform,
        publisher = game.publisher,
        isPhysical = game.isPhysical,
        timesCompleted = game.timesCompleted,
        completionDate = game.completionDate,
        coverImageUri = game.imageUri,
        isEdit = game.id.isNotEmpty(),
        isSavingGame = isLoading,
        onNameChange = { addGameViewModel.setGameName(it) },
        onShortNameChange = { addGameViewModel.setGameShortName(it) },
        onPublisherChange = { addGameViewModel.setGamePublisher(it) },
        onIsPhysicalChange = { addGameViewModel.setGameFormat(it) },
        onTimesCompletedChange = { addGameViewModel.setTimesCompleted(it) },
        onCompletionDateChange = { addGameViewModel.setCompletionDate(it) },
        onCoverImageChange = { addGameViewModel.setGameImageUri(it) },
        onSaveGame = { addGameViewModel.saveGame() },
        onBackPressed = onBackPressed
    )
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
    completionDate: String,
    coverImageUri: String = "",
    isEdit: Boolean,
    isSavingGame: Boolean,
    onNameChange: (String) -> Unit = { },
    onShortNameChange: (String) -> Unit = { },
    onPlatformChange: (String) -> Unit = { },
    onPublisherChange: (String) -> Unit = { },
    onIsPhysicalChange: (Boolean) -> Unit = { },
    onTimesCompletedChange: (Int) -> Unit = { },
    onCompletionDateChange: (String) -> Unit = { },
    onCoverImageChange: (Uri?) -> Unit = { },
    onSaveGame: () -> Unit = { },
    onBackPressed: () -> Unit = { }
) {

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),
                title = {
                    val actionBarTitle = if (isEdit) "Edit Game" else "Add New Game"
                    Text(text = actionBarTitle)
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description",
                            tint = Color.White
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (!isSavingGame) {
                FloatingActionButton(
                    onClick = onSaveGame,
                    shape = CircleShape,
                    containerColor = colorResource(id = R.color.colorAccent)
                ) {
                    if (isEdit) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.checked),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.colorAccent),
                    strokeWidth = 5.dp,
                    modifier = Modifier
                        .size(50.dp)
                )
            }
        }
    ) { innerPadding ->
        Box {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(color = Color.White)
                    .padding(innerPadding)
            ) {
                CoverImageSelector(
                    coverImageUri = coverImageUri,
                    onCoverImageChange = onCoverImageChange
                )

                EditGameForm(
                    name = name,
                    shortName = shortName,
                    platform = platform,
                    publisher = publisher,
                    isPhysical = isPhysical,
                    timesCompleted = timesCompleted,
                    completionDate = completionDate,
                    onNameChange = onNameChange,
                    onShortNameChange = onShortNameChange,
                    onPlatformChange = onPlatformChange,
                    onPublisherChange = onPublisherChange,
                    onIsPhysicalChange = onIsPhysicalChange,
                    onTimesCompletedChange = onTimesCompletedChange,
                    onCompletionDateChange = onCompletionDateChange
                )
            }
        }
    }
}

@Composable
fun CoverImageSelector(
    coverImageUri: String = "",
    onCoverImageChange: (Uri?) -> Unit = { },
) {
    val parsedImageUri = if (coverImageUri.isNotEmpty()) Uri.parse(coverImageUri) else null
    var selectedCover by rememberSaveable { mutableStateOf(parsedImageUri) }
    val pickMedia = rememberLauncherForActivityResult(contract = PickVisualMedia()) {
        it?.let { uri ->
            // TODO: Remove once other images no longer rely on Picasso
            Picasso.get().invalidate(coverImageUri)
            selectedCover = uri
            onCoverImageChange(uri)
        }
    }

    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedCover)
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
}

@Composable
fun EditGameForm(
    name: String,
    shortName: String,
    platform: String,
    publisher: String,
    isPhysical: Boolean,
    timesCompleted: Int,
    completionDate: String,
    onNameChange: (String) -> Unit = { },
    onShortNameChange: (String) -> Unit = { },
    onPlatformChange: (String) -> Unit = { },
    onPublisherChange: (String) -> Unit = { },
    onIsPhysicalChange: (Boolean) -> Unit = { },
    onTimesCompletedChange: (Int) -> Unit = { },
    onCompletionDateChange: (String) -> Unit = { },
) {
    val digitalTextBackground = if (!isPhysical) MaterialTheme.colorScheme.primaryContainer else Color.White
    val physicalTextBackground = if (isPhysical) MaterialTheme.colorScheme.primaryContainer else Color.White

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(all = 10.dp)
            .verticalScroll(rememberScrollState())
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
                enabled = false,
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
            Text(
                text = "Digital",
                modifier = Modifier
                    .background(digitalTextBackground, RoundedCornerShape(size = 50.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onIsPhysicalChange(false) }
            )
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
                            painter = painterResource(id = R.drawable.cloud),
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
            Text(
                text = "Physical",
                modifier = Modifier
                    .background(physicalTextBackground, RoundedCornerShape(size = 50.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onIsPhysicalChange(true) }
            )
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

        DatePickerField(
            label = "Completion date",
            initialDate = completionDate,
            onDateSelected = { onCompletionDateChange(it) }
        )

        Spacer(modifier = Modifier
            .height(50.dp)
            .fillMaxWidth())
    }
}

@Composable
fun DatePickerField(
    label: String = "Select date",
    initialDate: String? = null,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val displayedDateText = remember(initialDate) {
        initialDate?.let {
            try {
                Instant.parse(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e: Exception) {
                ""
            }
        }.orEmpty()
    }


    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                showDatePicker = true
            }
    ) {
        OutlinedTextField(
            value = displayedDateText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        )
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = {
                it?.let { instant ->
                    onDateSelected(instant.toString())
                }
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }
}

@Composable
fun DatePickerModal(
    onDateSelected: (Instant?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val userZone = ZoneId.systemDefault()
                val selectedUtcMillis = datePickerState.selectedDateMillis

                // Convert UTC millis to LocalDate *as UTC*, ignoring local zone
                val selectedUtcDate = selectedUtcMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                }

                // Reinterpret that LocalDate as being in local timezone
                val correctedInstant = selectedUtcDate?.atStartOfDay(userZone)?.toInstant()

                onDateSelected(correctedInstant)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
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
            isEdit = false,
            isSavingGame = false,
            timesCompleted = timesCompleted,
            completionDate = "",
            onIsPhysicalChange = { isPhysical = it },
            onTimesCompletedChange = { timesCompleted = it}
        )
    }
}