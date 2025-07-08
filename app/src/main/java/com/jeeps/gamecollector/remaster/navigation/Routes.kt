package com.jeeps.gamecollector.remaster.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.TransformOrigin
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.ui.gamePlatforms.AddPlatformScreen
import com.jeeps.gamecollector.remaster.ui.gamePlatforms.GamePlatformsScreen
import com.jeeps.gamecollector.remaster.ui.games.details.GameDetailsScreen
import com.jeeps.gamecollector.remaster.ui.games.edit.AddGameScreen
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformScreen
import com.jeeps.gamecollector.remaster.ui.userStats.UserStatsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf


sealed class Screen {
    @Serializable
    object GamePlatforms : Screen()
    @Serializable
    object UserStats : Screen()
    @Serializable
    data class AddPlatform(val platform: Platform? = null) : Screen()
    @Serializable
    data class GamesFromPlatform(val platformId: String, val platformName: String) : Screen()
    @Serializable
    data class GameDetails(val platformId: String? = null, val platformName: String? = null, val game: Game? = null) : Screen()
    @Serializable
    data class AddGame(val platformId: String? = null, val platformName: String? = null, val game: Game? = null) : Screen()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun Main() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.GamePlatforms,
            popEnterTransition = {
                EnterTransition.None
            },
            // TODO: Experiment to find the best transitions
            enterTransition = {
//            slideInHorizontally(animationSpec = tween(300)) { fullWidth ->
//                fullWidth / 4
//            }  + fadeIn(animationSpec = tween(300))
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
//            slideOutHorizontally(animationSpec = tween(300)) {
//                it / 4
//            } + fadeOut(animationSpec = tween(300))
//            fadeOut(animationSpec = tween(300))
                scaleOut(
                    targetScale = 0.9f,
                    transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
                ) + fadeOut(animationSpec = tween(300))
            },
            exitTransition = {
//            fadeOut(animationSpec = tween(600))
                ExitTransition.None
            }
        ) {
            composable<Screen.GamePlatforms> {
                GamePlatformsScreen(
                    onOpenStats = {
                        navController.navigate(Screen.UserStats)
                    },
                    onCreatePlatform = {
                        navController.navigate(Screen.AddPlatform())
                    },
                    onOpenPlatform = { platform ->
                        navController.navigate(Screen.GamesFromPlatform(
                            platformId = platform.id,
                            platformName = platform.name
                        ))
                    },
                    onEditPlatform = { platform ->
                        navController.navigate(Screen.AddPlatform(platform))
                    }
                )
            }
            composable<Screen.UserStats> {
                UserStatsScreen()
            }
            composable<Screen.AddPlatform>(
                typeMap = mapOf(
                    typeOf<Platform?>() to CustomNavType.PlatformType
                )
            ){ backStackEntry ->
                val route: Screen.AddPlatform = backStackEntry.toRoute()

                AddPlatformScreen(
                    initialPlatform = route.platform,
                    onBackPressed = {
                        navController.popBackStackOnResume()
                    }
                )
            }
            composable<Screen.GamesFromPlatform> { backStackEntry ->
                val route: Screen.GamesFromPlatform = backStackEntry.toRoute()

                GamesFromPlatformScreen(
                    animatedVisibilityScope = this@composable,
                    platformId = route.platformId,
                    platformName = route.platformName,
                    onBackPressed = { navController.popBackStackOnResume() },
                    onEditGame = { game ->
                        navController.navigate(Screen.GameDetails(
                            platformId = route.platformId,
                            platformName = route.platformName,
                            game = game
                        ))
                    },
                    onAddGame = {
                        navController.navigate(Screen.AddGame(
                            platformId = route.platformId,
                            platformName = route.platformName
                        ))
                    }
                )
            }
            composable<Screen.GameDetails>(
                typeMap = mapOf(
                    typeOf<Game?>() to CustomNavType.GameType
                )
            ) { backStackEntry ->
                val route: Screen.GameDetails = backStackEntry.toRoute()

                GameDetailsScreen(
                    animatedVisibilityScope = this@composable,
                    platformId = route.platformId,
                    platformName = route.platformName,
                    selectedGame = route.game,
                    onBackPressed = { navController.popBackStackOnResume() },
                    onEditGame = { game ->
                        navController.navigate(Screen.AddGame(
                            platformId = route.platformId,
                            platformName = route.platformName,
                            game = game
                        ))
                    }
                )
            }
            composable<Screen.AddGame>(
                typeMap = mapOf(
                    typeOf<Game?>() to CustomNavType.GameType
                )
            ) {
                val route: Screen.AddGame = it.toRoute()

                AddGameScreen(
                    platformId = route.platformId,
                    platformName = route.platformName,
                    selectedGame = route.game,
                    onBackPressed = { navController.popBackStackOnResume() },
                    onGameSaved = { message ->
                        // TODO: Notify user that game was saved
                        navController.popBackStack(route = Screen.GamesFromPlatform(
                            platformId = route.platformId.orEmpty(),
                            platformName = route.platformName.orEmpty()
                        ), inclusive = false)
                    }
                )
            }
        }
    }
}

fun NavHostController.popBackStackOnResume() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}