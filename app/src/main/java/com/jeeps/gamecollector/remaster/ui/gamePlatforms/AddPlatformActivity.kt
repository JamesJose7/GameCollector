package com.jeeps.gamecollector.remaster.ui.gamePlatforms

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityAddPlatformBinding
import com.jeeps.gamecollector.deprecated.utils.PlatformColor
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.composables.ObserveAsEvents
import com.jeeps.gamecollector.remaster.ui.gamePlatforms.AddPlatformViewModel.UiEvent
import com.jeeps.gamecollector.remaster.ui.games.edit.CoverImageSelector
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import com.jeeps.gamecollector.remaster.utils.extensions.colorFromHexString
import com.jeeps.gamecollector.remaster.utils.extensions.serializable
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddPlatformActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAddPlatformBinding::inflate)

    private val viewModel: AddPlatformViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Get platform if being edited
        val platform = intent?.serializable<Platform>(EDITED_PLATFORM)
        platform?.let {
            viewModel.setPlatform(it)
            viewModel.isEdit = true
        }

        binding.screenCompose.setComposable {
            AddPlatformScreen(
                viewModel = viewModel,
                onBackPressed = { onBackPressedDispatcher.onBackPressed() }
            )
        }
    }

    companion object {
        const val EDITED_PLATFORM = "EDITED PLATFORM"
        const val EDITED_PLATFORM_POSITION = "EDITED PLATFORM POSITION"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AddPlatformScreen(
    viewModel: AddPlatformViewModel = hiltViewModel(),
    initialPlatform: Platform? = null,
    onBackPressed: () -> Unit
) {
    val isLoading by viewModel.isLoading.observeAsState(false)
    val platform by viewModel.platform.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun showSnackbarMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message)
        }
    }

    LaunchedEffect(initialPlatform) {
        initialPlatform?.let {
            viewModel.setPlatform(it)
            viewModel.isEdit = true
        }
    }

    ObserveAsEvents(viewModel.messageEventsChannelFlow) { event ->
        when (event) {
            is BaseViewModel.MessageEvent.Error -> {
                showSnackbarMessage(event.message)
            }
            is BaseViewModel.MessageEvent.Success -> {
                showSnackbarMessage(event.message)
            }
        }
    }

    ObserveAsEvents(viewModel.uiEventsChannelFlow) { event ->
        when (event) {
            is UiEvent.ShowFieldError -> {
                showSnackbarMessage(event.message)
            }
            is UiEvent.ImageFinishedUploading -> {
                onBackPressed()
            }
        }
    }

    AddPlatformScreen(
        snackbarHostState = snackbarHostState,
        isLoading = isLoading,
        platformImageUri = platform.imageUri,
        name = platform.name,
        selectedColor = PlatformColor.fromColor(platform.color),
        onPlatformImageChange = { viewModel.setPlatformImageUri(it) },
        onBackPressed = onBackPressed,
        onSavePlatform = { viewModel.savePlatform() },
        onNameChange = { viewModel.setPlatformName(it) },
        onColorChange = { viewModel.setPlatformColor(it.color) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlatformScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean,
    platformImageUri: String = "",
    name: String,
    selectedColor: PlatformColor,
    onPlatformImageChange: (Uri?) -> Unit = { },
    onBackPressed: () -> Unit = {},
    onSavePlatform: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onColorChange: (PlatformColor) -> Unit = {}
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(text = stringResource(R.string.add_platform))
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
            if (!isLoading) {
                FloatingActionButton(
                    onClick = onSavePlatform,
                    shape = CircleShape,
                    containerColor = colorResource(id = R.color.colorAccent)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.checked),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
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
        Column(
            modifier = modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            CoverImageSelector(
                coverImageUri = platformImageUri,
                onCoverImageChange = onPlatformImageChange
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(all = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(text = "Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Color",
                    color = colorResource(id = R.color.textSecondaryColor),
                    modifier = Modifier
                )
                ColorRadioGroup(
                    selectedColor = selectedColor,
                    onColorChange = onColorChange
                )
            }
        }
    }
}

@Composable
fun ColorRadioGroup(
    selectedColor: PlatformColor,
    onColorChange: (PlatformColor) -> Unit
) {
    val options = PlatformColor.entries

    Column {
        options.forEach { platformColor ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onColorChange(platformColor) }
                    .padding(4.dp)
            ) {
                RadioButton(
                    selected = (platformColor == selectedColor),
                    onClick = { onColorChange(platformColor) }
                )
                Text(
                    text = stringResource(platformColor.stringId),
                    color = if (platformColor == PlatformColor.NORMIE_WHITE) {
                        colorResource(R.color.textColorPrimary)
                    } else {
                        platformColor.color.colorFromHexString()
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun AddPlatformScreenPreview() {
    AppTheme {
        AddPlatformScreen(
            snackbarHostState = SnackbarHostState(),
            isLoading = false,
            name = "Switch",
            selectedColor = PlatformColor.SWITCH_RED
        )
    }
}