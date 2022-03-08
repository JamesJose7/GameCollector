package com.jeeps.gamecollector.remaster.ui.games.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jeeps.gamecollector.model.Game
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddGameViewModel @Inject constructor(

) : BaseViewModel() {

    private var timesCompleted: Int = 0

    private val _selectedGame = MutableLiveData<Game>()
    val selectedGame: LiveData<Game>
        get() = _selectedGame

    var selectedGamePosition: Int = -1
    var platformName: String? = null
    var platformId: String? = null

    var currentImageUri: Uri? = null
    var coverDeleted: Boolean = false

    fun setSelectedGame(game: Game) {
        _selectedGame.value = game
    }

    fun setTimesCompleted(value: Int) {
        timesCompleted = value
        _selectedGame.value?.timesCompleted = value
    }

    fun setGameFormat(isPhysical: Boolean) {
        _selectedGame.value?.setPhysical(isPhysical)
    }

    fun setGameName(name: String) {
        _selectedGame.value?.name = name
    }

    fun setGameShortName(shortName: String) {
        _selectedGame.value?.shortName = shortName
    }

    fun setGamePublisher(publisher: String) {
        _selectedGame.value?.publisher = publisher
    }

    fun setGameImageUri(uri: Uri?) {
        currentImageUri = uri
        _selectedGame.value?.imageUri = uri?.toString() ?: ""
    }

    fun initializeDefaultGame() {
        val game = Game(
            "",
        true,
        "",
        "",
        platformId ?: "",
        platformName ?: "",
        "",
        ""
        )
        setSelectedGame(game)
    }
}