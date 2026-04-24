package com.jeeps.gamecollector.remaster.ui.games.details

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.games.addAdditionalGameDetails
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.igdb.findMostSimilarGame
import com.jeeps.gamecollector.remaster.data.model.data.igdb.toNames
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.data.repository.IgdbRepository
import com.jeeps.gamecollector.remaster.data.repository.UserStatsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.remaster.utils.IgdbUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@ExperimentalCoroutinesApi
@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val gamesRepository: GamesRepository,
    private val igdbRepository: IgdbRepository,
    private val statsRepository: UserStatsRepository
) : BaseViewModel() {

    data class GameDetailsUiState(
        val selectedGame: Game? = null,
        val gameMainColor: Color? = null,
        val gameHoursStats: GameplayHoursStats = GameplayHoursStats(),
        val showHoursErrorMessage: Boolean = false,
        val isLoadingGameHours: Boolean = false,
        val isLoadingCompletionUpdate: Boolean = false,
        val games: List<Game> = emptyList()
    )

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    var platformId: String = ""
        set(value) {
            field = value
            getUserGames()
        }

    fun setSelectedGame(game: Game) {
        // TODO: Check if this is still needed
        val gameToSet = game.copy(currentSortStat = "")

        _uiState.update {
            it.copy(
                selectedGame = gameToSet,
                gameHoursStats = GameplayHoursStats(gameToSet.gameHoursStats)
            )
        }
        getColorBasedOnCover()
        checkIfGameHasHoursStats(gameToSet.gameHoursStats)
        updateGameDetails()
    }

    private fun getColorBasedOnCover() {
        viewModelScope.launch {
            decodeBitmapUrl()
        }
    }

    private suspend fun decodeBitmapUrl() {
        withContext(Dispatchers.IO) {
            _uiState.value.selectedGame?.let { game ->
                kotlin.runCatching {
                    val url = URL(game.imageUri)
                    val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    val palette = Palette.from(image).generate()
                    val mainColor = palette.getDominantColor("#3F51B5".toColorInt())
                    _uiState.update { it.copy(gameMainColor = Color(mainColor)) }
                }.onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }

    fun updateGameCompletion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCompletionUpdate = true) }
            _uiState.value.selectedGame?.id?.let { gameId ->
                handleNetworkResponse(gamesRepository.toggleGameCompletion(gameId)) {
                    val isCompleted = it.completed
                    val message =
                        if (isCompleted) "Marked as complete"
                        else "Marked as incomplete"
                    postServerMessage(message)

                    val timesCompleted = if (isCompleted) 1 else 0
                    _uiState.update { state ->
                        state.copy(selectedGame = state.selectedGame?.copy(timesCompleted = timesCompleted))
                    }
                }
            }
            _uiState.update { it.copy(isLoadingCompletionUpdate = false) }
        }
    }

    private fun checkIfGameHasHoursStats(gameHoursStats: GameHoursStats) {
        val emptyHoursStats = GameHoursStats()
        if (gameHoursStats == emptyHoursStats) {
            getGameHours()
        }
    }

    fun getGameHours() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGameHours = true) }
            _uiState.value.selectedGame?.let { game ->
                handleNetworkResponse(statsRepository.getGameHours(game.name),
                    { stats ->
                        if (isStoredHoursDifferentFromIgbd(game.gameHoursStats, stats)) {
                            updateGameHours(stats, game.id)
                        }
                        _uiState.update {
                            it.copy(
                                gameHoursStats = stats,
                                showHoursErrorMessage = false,
                                isLoadingGameHours = false
                            )
                        }
                    }, {
                        _uiState.update {
                            it.copy(
                                showHoursErrorMessage = true,
                                isLoadingGameHours = false
                            )
                        }
                    })
            }
        }
    }

    private fun updateGameHours(stats: GameplayHoursStats, gameId: String) {
        viewModelScope.launch {
            when (val updateGameHours = gamesRepository.updateGameHours(stats, gameId)) {
                is State.Failed -> handleError(ErrorType.SERVER_ERROR, updateGameHours.e)
                else -> {}
            }
        }
    }

    private fun isStoredHoursDifferentFromIgbd(
        storedHours: GameHoursStats,
        igdbHours: GameplayHoursStats
    ): Boolean {
        return storedHours.gameplayCompletionist != igdbHours.gameplayCompletionist ||
                storedHours.gameplayMain != igdbHours.gameplayMain ||
                storedHours.gameplayMainExtra != igdbHours.gameplayMainExtra
    }

    private fun updateGameDetails() {
        val game = _uiState.value.selectedGame ?: return
        if (game.url.isNotEmpty() && game.genresNames.isNotEmpty()) return

        viewModelScope.launch {
            val igdbGames =
                handleNetworkResponse(igdbRepository.searchGames(IgdbUtils.getSearchGamesQuery(game.name)))
            igdbGames.findMostSimilarGame(game.name)?.let { gameIG ->
                val genres = gameIG.genres
                    ?.let { handleNetworkResponse(igdbRepository.getGenresByIds(IgdbUtils.getGameGenresQuery(it))) }
                    ?: emptyList()

                val genreNames = genres.toNames()
                val updatedGame = game.addAdditionalGameDetails(
                    gameIG,
                    genreNames.ifEmpty { game.genresNames }
                )

                handleNetworkResponse(gamesRepository.editGame(updatedGame.id, updatedGame)) {
                    _uiState.update { state -> state.copy(selectedGame = updatedGame) }
                }
            }
        }
    }

    private fun getUserGames() {
        val user = authenticationRepository.getUser() ?: return

        viewModelScope.launch {
            gamesRepository.getUserGamesByPlatform(
                user.username,
                platformId
            ).collect { state ->
                when (state) {
                    is State.Loading -> startLoading()
                    is State.Success -> {
                        stopLoading()
                        state.data.let { result ->
                            result.let { _uiState.update { state -> state.copy(games = it) } }
                        }
                    }
                    is State.Failed -> {
                        stopLoading()
                        handleError(ErrorType.SERVER_ERROR, state.e)
                    }
                }
            }
        }
    }
}