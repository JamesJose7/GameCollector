package com.jeeps.gamecollector.remaster.ui.games.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.addAdditionalGameDetails
import com.jeeps.gamecollector.remaster.data.model.data.igdb.findMostSimilarGame
import com.jeeps.gamecollector.remaster.data.model.data.igdb.toNames
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.data.repository.IgdbRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.utils.Event
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.remaster.utils.getCurrentTimeInUtcString
import com.jeeps.gamecollector.remaster.utils.IgdbUtils
import com.jeeps.gamecollector.remaster.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AddGameViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val gamesRepository: GamesRepository,
    private val igdbRepository: IgdbRepository,
    private val imageCompressor: ImageCompressor
) : BaseViewModel() {

    private var timesCompleted: Int = 0

    private val _selectedGame = MutableStateFlow(Game())
    val selectedGame: StateFlow<Game> = _selectedGame.asStateFlow()

    var selectedGamePosition: Int = -1
    var platformName: String? = null
    var platformId: String? = null

    var currentImageUri: Uri? = null
    private var coverDeleted: Boolean = false

    private var pendingMessage: String = ""

    fun setSelectedGame(game: Game) {
        _selectedGame.value = game
    }

    fun setTimesCompleted(value: Int) {
        timesCompleted = value
        _selectedGame.value = _selectedGame.value.copy(timesCompleted = value)
    }

    fun setCompletionDate(value: String) {
        _selectedGame.value = _selectedGame.value.copy(completionDate = value)
    }

    fun setGameFormat(isPhysical: Boolean) {
        _selectedGame.value = _selectedGame.value.copy(isPhysical = isPhysical)
    }

    fun setGameName(name: String) {
        _selectedGame.value = _selectedGame.value.copy(name = name)
    }

    fun setGameShortName(shortName: String) {
        _selectedGame.value = _selectedGame.value.copy(shortName = shortName)
    }

    fun setGamePublisher(publisher: String) {
        _selectedGame.value = _selectedGame.value.copy(publisher = publisher)
    }

    fun setGameImageUri(uri: Uri?) {
        coverDeleted = uri == null
        currentImageUri = uri
        _selectedGame.value = _selectedGame.value.copy(imageUri = uri?.toString() ?: "")
    }

    fun checkIfGameIsBeingEdited() {
        if (selectedGame.value.id.isEmpty()) {
            initializeDefaultGame()
        }
    }

    private fun initializeDefaultGame() {
        val game = Game(
            "",
            true,
            "",
            "",
            platformId ?: "",
            platformName ?: "",
            "",
            ""
        ).apply {
            dateAdded = getCurrentTimeInUtcString()
        }
        setSelectedGame(game)
    }

    fun saveGame() {
        selectedGame.value.let { game ->
            val isEdit = game.id.isNotEmpty()
            when {
                !isEdit && currentImageUri == null -> {
                    saveGameAfterGettingCover(game, isEdit)
                }
                isEdit && coverDeleted -> {
                    saveGameAfterGettingCover(game, isEdit)
                }
                isEdit -> {
                    editGame(game)
                }
                else -> {
                    saveNewGame(game)
                }
            }
        }
    }

    private fun saveNewGame(game: Game) {
        viewModelScope.launch {
            startLoading()
            val token = authenticationRepository.getUserToken()
            handleNetworkResponse(gamesRepository.saveNewGame(token, game)) {
                if (currentImageUri != null) {
                    setSelectedGame(it)
                    pendingMessage = "Game created successfully"
                    currentImageUri?.let { uri ->
                        uploadCoverImage(imageCompressor.compressImage(uri))
                    }
                } else {
                    postServerMessage("Game created successfully")
                    stopLoading()
                }
            }
        }
    }

    private fun editGame(game: Game) {
        viewModelScope.launch {
            startLoading()
            val token = authenticationRepository.getUserToken()

            handleNetworkResponse(gamesRepository.editGame(token, game.id, game)) {
                if (currentImageUri != null) {
                    pendingMessage = "Game edited successfully"
                    currentImageUri?.let { uri ->
                        uploadCoverImage(imageCompressor.compressImage(uri))
                    }
                } else {
                    postServerMessage("Game edited successfully")
                    stopLoading()
                }
            }
        }
    }

    private fun saveGameAfterGettingCover(game: Game, isEdit: Boolean) {
        viewModelScope.launch {
            startLoading()
            val igdbGames =
                handleNetworkResponse(igdbRepository.searchGames(IgdbUtils.getSearchGamesQuery(game.name)))
            val selectedGame = igdbGames.findMostSimilarGame(_selectedGame.value.name)

            if (selectedGame == null) {
                continueSavingGame(isEdit, game)
            } else {
                val genres = selectedGame.genres
                    ?.let { handleNetworkResponse(igdbRepository.getGenresByIds(IgdbUtils.getGameGenresQuery(it))) }
                    ?: emptyList()

                game.addAdditionalGameDetails(selectedGame, genres.toNames())
                // Get image cover
                when (val response = igdbRepository
                    .getGameCoverById(IgdbUtils.getCoverImageQuery(selectedGame.cover))) {
                    is NetworkResponse.Success -> {
                        val gameCovers = response.body
                        if (gameCovers.isNotEmpty()) {
                            gameCovers[0].getBigCoverUrl().let { coverUrl ->
                                _selectedGame.value.imageUri = coverUrl
                                currentImageUri = null
                            }
                        }
                        continueSavingGame(isEdit, game)
                    }
                    is NetworkResponse.Error -> {
                        stopLoading()
                        handleError(response)
                        continueSavingGame(isEdit, game)
                    }
                }
            }
        }
    }

    private fun continueSavingGame(isEdit: Boolean, game: Game) {
        if (isEdit) editGame(game) else saveNewGame(game)
    }

    private fun uploadCoverImage(imageFile: File?) {
        viewModelScope.launch {
            startLoading()
            imageFile?.let { image ->
                val token = authenticationRepository.getUserToken()
                val requestFile = image
                    .asRequestBody("image/png".toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("image", image.name, requestFile)

                handleNetworkResponse(
                    gamesRepository
                        .uploadGameCover(token, selectedGame.value.id, body)
                ) {
                    postServerMessage(pendingMessage)
                }
                if (image.exists()) {
                    image.delete()
                }
            }
            stopLoading()
        }
    }
}