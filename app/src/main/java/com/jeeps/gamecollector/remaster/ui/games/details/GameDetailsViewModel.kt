package com.jeeps.gamecollector.remaster.ui.games.details

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.GameHoursStats
import com.jeeps.gamecollector.remaster.data.model.data.hltb.GameplayHoursStats
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.data.repository.UserStatsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
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
    private val statsRepository: UserStatsRepository
) : BaseViewModel() {

    private val _selectedGame = MutableLiveData<Game>()
    val selectedGame: LiveData<Game>
        get() = _selectedGame

    private val _gameMainColor = MutableLiveData<Int>()
    val gameMainColor: LiveData<Int>
        get() = _gameMainColor

    private val _gameHoursStats = MutableLiveData<GameplayHoursStats>()
    val gameHoursStats: LiveData<GameplayHoursStats>
        get() = _gameHoursStats

    var selectedGamePosition: Int = -1
    var platformName: String? = null
    var platformId: String? = null

    fun setSelectedGame(game: Game) {
        _selectedGame.value = game
        _gameHoursStats.value = GameplayHoursStats(game.gameHoursStats)
        getGameHours()
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
            selectedGame.value?.id?.let {  gameId ->
                when (val response = gamesRepository.toggleGameCompletion(token, gameId)) {
                    is NetworkResponse.Success -> {
                        val isCompleted = response.body.isCompleted
                        val message =
                            if (isCompleted) "Marked as complete"
                            else "Marked as incomplete"
                        postServerMessage(message)

                        _selectedGame.value?.timesCompleted = if (isCompleted) 1 else 0
                    }
                    is NetworkResponse.ServerError -> {
                        handleError(ErrorType.SERVER_ERROR, response.error)
                    }
                    is NetworkResponse.NetworkError -> {
                        handleError(ErrorType.NETWORK_ERROR, response.error)
                    }
                    is NetworkResponse.UnknownError -> {
                        handleError(ErrorType.UNKNOWN_ERROR, response.error)
                    }
                }
            }
        }
    }

    private fun getGameHours() {
        viewModelScope.launch {
            _selectedGame.value?.let { game ->
                when (val response = statsRepository.getGameHours(game.name)) {
                    is NetworkResponse.Success -> {
                        val stats = response.body
                        if (isStoredHoursDifferentFromIgbd(game.gameHoursStats, stats)) {
                            _gameHoursStats.value = stats
                            updateGameHours(stats, game.id)
                        }
                    }
                    is NetworkResponse.ServerError -> {
                        handleError(ErrorType.SERVER_ERROR, response.error)
                    }
                    is NetworkResponse.NetworkError -> {
                        handleError(ErrorType.NETWORK_ERROR, response.error)
                    }
                    is NetworkResponse.UnknownError -> {
                        handleError(ErrorType.UNKNOWN_ERROR, response.error)
                    }
                }
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

    private fun isStoredHoursDifferentFromIgbd(storedHours: GameHoursStats, igdbHours: GameplayHoursStats): Boolean {
        return storedHours.gameplayCompletionist != igdbHours.gameplayCompletionist ||
                storedHours.gameplayMain != igdbHours.gameplayMain ||
                storedHours.gameplayMainExtra != igdbHours.gameplayMainExtra
    }
}