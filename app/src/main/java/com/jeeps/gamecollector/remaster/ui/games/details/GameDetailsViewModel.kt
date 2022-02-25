package com.jeeps.gamecollector.remaster.ui.games.details

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.model.Game
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.net.URL
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val gamesRepository: GamesRepository
) : BaseViewModel() {

    private val _selectedGame = MutableLiveData<Game>()
    val selectedGame: LiveData<Game>
        get() = _selectedGame

    private val _gameMainColor = MutableLiveData<Int>()
    val gameMainColor: LiveData<Int>
        get() = _gameMainColor

    var selectedGamePosition: Int = -1
    var platformName: String? = null
    var platformId: String? = null

    fun setSelectedGame(game: Game) {
        _selectedGame.value = game
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
                        _selectedGame.value = _selectedGame.value
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
}