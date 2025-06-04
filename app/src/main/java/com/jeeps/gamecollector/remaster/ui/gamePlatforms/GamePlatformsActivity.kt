package com.jeeps.gamecollector.remaster.ui.gamePlatforms

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityMainLibraryBinding
import com.jeeps.gamecollector.deprecated.MainLibraryActivity
import com.jeeps.gamecollector.deprecated.PlatformLibraryActivity
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.composables.MenuItem
import com.jeeps.gamecollector.remaster.ui.composables.ObserveAsEvents
import com.jeeps.gamecollector.remaster.ui.composables.PopUpMenu
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity
import com.jeeps.gamecollector.remaster.ui.login.LoginActivity
import com.jeeps.gamecollector.remaster.ui.userStats.UserStatsActivity
import com.jeeps.gamecollector.remaster.utils.extensions.colorFromHexString
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.jeeps.gamecollector.remaster.utils.extensions.showSnackBar
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GamePlatformsActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMainLibraryBinding::inflate)

    private val viewModel: GamePlatformsViewModel by viewModels()

    private val addPlatformResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleAddPlatformResult(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.screenCompose.setComposable {
            GamePlatformsScreen(
                viewModel = viewModel,
                onOpenStats = {
                    val intent = Intent(this, UserStatsActivity::class.java)
                    startActivity(intent)
                },
                onCreatePlatform = {
                    intent = Intent(this, AddPlatformActivity::class.java)
                    addPlatformResultLauncher.launch(intent)
                },
                onOpenPlatform = { platform ->
                    val intent = Intent(this, GamesFromPlatformActivity::class.java).apply {
                        putExtra(PlatformLibraryActivity.CURRENT_PLATFORM, platform.id)
                        putExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME, platform.name)
                    }
                    startActivity(intent)
                },
                onEditPlatform = { platform ->
                    val intent = Intent(this, AddPlatformActivity::class.java).apply {
                        putExtra(AddPlatformActivity.EDITED_PLATFORM, platform)
                    }
                    startActivityForResult(
                        intent,
                        MainLibraryActivity.EDIT_PLATFORM_RESULT
                    )
                }
            )
        }
    }

    private fun handleAddPlatformResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            showSnackBar(binding.root, "Successfully added platform")
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun GamePlatformsScreen(
    viewModel: GamePlatformsViewModel = viewModel(),
    onOpenStats: () -> Unit,
    onCreatePlatform: () -> Unit,
    onOpenPlatform: (Platform) -> Unit,
    onEditPlatform: (Platform) -> Unit
) {
    val isLoading by viewModel.isLoading.observeAsState(true)
    val platforms by viewModel.platforms.observeAsState(emptyList())
    val isUserLoggedIn by viewModel.isUserLoggedIn.observeAsState(true)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSnackbarMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message)
        }
    }

    fun promptLogin() {
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            promptLogin()
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
            GamePlatformsViewModel.UiEvent.PromptLogin -> promptLogin()
        }
    }

    GamePlatformsScreen(
        snackbarHostState = snackbarHostState,
        isLoading = isLoading,
        platforms = platforms,
        onOpenStats = onOpenStats,
        onLogout = { viewModel.logout() },
        onCreatePlatform = onCreatePlatform,
        onOpenPlatform = onOpenPlatform,
        onEditPlatform = onEditPlatform
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlatformsScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean,
    platforms: List<Platform>,
    onOpenStats: () -> Unit = {},
    onLogout: () -> Unit = {},
    onCreatePlatform: () -> Unit = {},
    onOpenPlatform: (Platform) -> Unit = {},
    onEditPlatform: (Platform) -> Unit = {}
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val dismissMenu = { isMenuExpanded = false }

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
                    Text(text = stringResource(R.string.title_activity_add_platform))
                },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            painter = painterResource(R.drawable.stats_icon),
                            contentDescription = "stats",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "popup menu",
                            tint = Color.White
                        )

                        PopUpMenu(
                            expanded = isMenuExpanded,
                            onDismiss = dismissMenu,
                            menuItems = listOf(
                                MenuItem(
                                    text = stringResource(R.string.log_out),
                                    onClick = {
                                        onLogout()
                                        dismissMenu()
                                    }
                                )
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePlatform,
                shape = CircleShape,
                containerColor = colorResource(id = R.color.colorAccent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.colorAccent),
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(platforms, key = { it.id }) { platform ->
                PlatformCard(
                    platform = platform,
                    onClick = { onOpenPlatform(platform) },
                    onLongClick = { onEditPlatform(platform) }
                )
            }
        }

    }
}

@Composable
fun PlatformCard(
    modifier: Modifier = Modifier,
    platform: Platform,
    onClick: (Platform) -> Unit,
    onLongClick: (Platform) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        modifier = modifier
            .height(200.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(platform) },
                onLongClick = { onLongClick(platform) }
            )
    ) {
        Column(
            modifier = Modifier
                .background(platform.color.colorFromHexString())
        ) {
            AsyncImage(
                model = platform.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.game_controller),
                error = painterResource(R.drawable.game_controller),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(CutCornerShape(topStart = 34.dp))
                    .background(Color.LightGray)
            )
            Text(
                text = platform.name,
                fontSize = 30.sp,
                color = colorResource(R.color.textColorPrimary),
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun GamePlatformsScreenPreview() {
    val platforms = listOf(
        Platform(
            id = "1",
            name = "Nintendo Switch",
            imageUri = "",
            color = "#ff0000"
        ),
        Platform(
            id = "2",
            name = "PS5",
            imageUri = "",
            color = "#ff00ff"
        )
    )

    AppTheme {
        GamePlatformsScreen(
            snackbarHostState = SnackbarHostState(),
            isLoading = false,
            platforms = platforms
        )
    }
}


@Preview
@Composable
private fun GamePlatformsScreenLoadingPreview() {
    AppTheme {
        GamePlatformsScreen(
            snackbarHostState = SnackbarHostState(),
            isLoading = true,
            platforms = emptyList()
        )
    }
}