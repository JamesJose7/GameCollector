package com.jeeps.gamecollector.remaster.ui.games.details

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.games.addAdditionalGameDetails
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.igdb.findMostSimilarGame
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.data.repository.IgdbRepository
import com.jeeps.gamecollector.remaster.data.repository.UserStatsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.utils.IgdbUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val gamesRepository: GamesRepository,
    private val igdbRepository: IgdbRepository,
    private val statsRepository: UserStatsRepository
) : BaseViewModel() {

    var toolbarAnimationStarted = false

    private val _selectedGame = MutableLiveData<Game>()
    val selectedGame: LiveData<Game>
        get() = _selectedGame

    private val _gameMainColor = MutableLiveData<Int>()
    val gameMainColor: LiveData<Int>
        get() = _gameMainColor

    private val _gameHoursStats = MutableLiveData<GameplayHoursStats>()
    val gameHoursStats: LiveData<GameplayHoursStats>
        get() = _gameHoursStats

    private val _showHoursErrorMessage = MutableLiveData(false)
    val showHoursErrorMessage: LiveData<Boolean>
        get() = _showHoursErrorMessage

    var selectedGamePosition: Int = -1
    var platformName: String? = null
    var platformId: String? = null

    fun setSelectedGame(game: Game) {
        _selectedGame.value = game
        _gameHoursStats.value = GameplayHoursStats(game.gameHoursStats)
        getGameHours()
        updateGameDetails()
    }

    fun getColorBasedOnCover() {
        viewModelScope.launch {
            decodeBitmapUrl()
        }
    }

    private suspend fun decodeBitmapUrl() {
        withContext(Dispatchers.IO) {
            selectedGame.value?.let { game ->
                kotlin.runCatching {
                    val url = URL(game.imageUri)
                    val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    val palette = Palette.from(image).generate()
                    val mainColor = palette.getDominantColor(Color.parseColor("#3F51B5"))
                    _gameMainColor.postValue(mainColor)
                }.onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }

    fun updateGameCompletion() {
        viewModelScope.launch {
            val token = authenticationRepository.getUserToken()
            selectedGame.value?.id?.let { gameId ->
                handleNetworkResponse(gamesRepository.toggleGameCompletion(token, gameId)) {
                    val isCompleted = it.completed
                    val message =
                        if (isCompleted) "Marked as complete"
                        else "Marked as incomplete"
                    postServerMessage(message)

                    _selectedGame.value?.timesCompleted = if (isCompleted) 1 else 0
                }
            }
        }
    }

    private fun getGameHours() {
        viewModelScope.launch {
            _selectedGame.value?.let { game ->
                handleNetworkResponse(statsRepository.getGameHours(game.name),
                    { stats ->
                        if (isStoredHoursDifferentFromIgbd(game.gameHoursStats, stats)) {
                            _gameHoursStats.value = stats
                            updateGameHours(stats, game.id)
                        }
                        _showHoursErrorMessage.postValue(false)
                    }, {
                        _showHoursErrorMessage.postValue(true)
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

    private fun updateGameDetails() = _selectedGame.value?.let { game ->
        if (game.url.isNotEmpty()) return@let

        viewModelScope.launch {
            val igdbGames =
                handleNetworkResponse(igdbRepository.searchGames(IgdbUtils.getSearchGamesQuery(game.name)))
            igdbGames.findMostSimilarGame(game.name)?.let { gameIG ->
                game.addAdditionalGameDetails(gameIG)

                val token = authenticationRepository.getUserToken()
                handleNetworkResponse(gamesRepository.editGame(token, game.id, game)) {
                    _selectedGame.value = game
                }
            }
        }
    }

}