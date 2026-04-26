package com.jeeps.gamecollector.remaster.ui.games.edit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.addAdditionalGameDetails
import com.jeeps.gamecollector.remaster.data.model.data.igdb.findMostSimilarGame
import com.jeeps.gamecollector.remaster.data.model.data.igdb.toNames
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.data.repository.IgdbRepository
import com.jeeps.gamecollector.remaster.navigation.CustomNavType
import com.jeeps.gamecollector.remaster.navigation.Screen
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.remaster.utils.getCurrentTimeInUtcString
import com.jeeps.gamecollector.remaster.utils.IgdbUtils
import com.jeeps.gamecollector.remaster.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.reflect.typeOf

@HiltViewModel
class AddGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gamesRepository: GamesRepository,
    private val igdbRepository: IgdbRepository,
    private val imageCompressor: ImageCompressor
) : BaseViewModel() {

    data class UiState(
        val game: Game = Game(),
        val currentImageUri: Uri? = null,
        val isImageDeleted: Boolean = false,
        val pendingMessage: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val route = savedStateHandle.toRoute<Screen.AddGame>(
            typeMap = mapOf(typeOf<Game?>() to CustomNavType.GameType)
        )
        val platformId = route.platformId
        val platformName = route.platformName

        val initialGame = route.game ?: createDefaultGame(platformId, platformName)
        _uiState.update { it.copy(game = initialGame) }
    }

    private fun createDefaultGame(platformId: String?, platformName: String?): Game {
        return Game(
            isPhysical = true,
            platformId = platformId ?: "",
            platform = platformName ?: "",
            dateAdded = getCurrentTimeInUtcString()
        )
    }

    fun setTimesCompleted(value: Int) {
        _uiState.update { it.copy(game = it.game.copy(timesCompleted = value)) }
    }

    fun setCompletionDate(value: String) {
        _uiState.update { it.copy(game = it.game.copy(completionDate = value)) }
    }

    fun setGameFormat(isPhysical: Boolean) {
        _uiState.update { it.copy(game = it.game.copy(isPhysical = isPhysical)) }
    }

    fun setGameName(name: String) {
        _uiState.update { it.copy(game = it.game.copy(name = name)) }
    }

    fun setGameShortName(shortName: String) {
        _uiState.update { it.copy(game = it.game.copy(shortName = shortName)) }
    }

    fun setGamePublisher(publisher: String) {
        _uiState.update { it.copy(game = it.game.copy(publisher = publisher)) }
    }

    fun setGameImageUri(uri: Uri?) {
        _uiState.update {
            it.copy(
                currentImageUri = uri,
                isImageDeleted = uri == null,
                game = it.game.copy(imageUri = uri?.toString() ?: "")
            )
        }
    }

    fun saveGame() {
        val state = _uiState.value
        val game = state.game
        val isEdit = game.id.isNotEmpty()
        
        when {
            !isEdit && state.currentImageUri == null -> {
                saveGameAfterGettingCover(game, false)
            }
            isEdit && state.isImageDeleted -> {
                saveGameAfterGettingCover(game, true)
            }
            isEdit -> {
                editGame(game)
            }
            else -> {
                saveNewGame(game)
            }
        }
    }

    private fun saveNewGame(game: Game) {
        viewModelScope.launch {
            startLoading()
            handleNetworkResponse(gamesRepository.saveNewGame(game)) { newGame ->
                if (_uiState.value.currentImageUri != null) {
                    _uiState.update { 
                        it.copy(
                            game = newGame,
                            pendingMessage = "Game created successfully"
                        )
                    }
                    _uiState.value.currentImageUri?.let { uri ->
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
            handleNetworkResponse(gamesRepository.editGame(game.id, game)) {
                if (_uiState.value.currentImageUri != null) {
                    _uiState.update { it.copy(pendingMessage = "Game edited successfully") }
                    _uiState.value.currentImageUri?.let { uri ->
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
            val selectedIgdbGame = igdbGames.findMostSimilarGame(game.name)

            if (selectedIgdbGame == null) {
                continueSavingGame(isEdit, game)
            } else {
                val genres = selectedIgdbGame.genres
                    ?.let { handleNetworkResponse(igdbRepository.getGenresByIds(IgdbUtils.getGameGenresQuery(it))) }
                    ?: emptyList()

                var updatedGame = game.addAdditionalGameDetails(selectedIgdbGame, genres.toNames())
                // Get image cover
                when (val response = igdbRepository
                    .getGameCoverById(IgdbUtils.getCoverImageQuery(selectedIgdbGame.cover))) {
                    is NetworkResponse.Success -> {
                        val gameCovers = response.body
                        if (gameCovers.isNotEmpty()) {
                            gameCovers[0].getBigCoverUrl().let { coverUrl ->
                                updatedGame = updatedGame.copy(imageUri = coverUrl)
                                _uiState.update { 
                                    it.copy(
                                        game = updatedGame, 
                                        currentImageUri = null
                                    ) 
                                }
                            }
                        }
                        continueSavingGame(isEdit, updatedGame)
                    }
                    is NetworkResponse.Error -> {
                        stopLoading()
                        handleError(response)
                        continueSavingGame(isEdit, updatedGame)
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
                val requestFile = image
                    .asRequestBody("image/png".toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("image", image.name, requestFile)

                handleNetworkResponse(
                    gamesRepository
                        .uploadGameCover(_uiState.value.game.id, body)
                ) {
                    postServerMessage(_uiState.value.pendingMessage)
                }
                if (image.exists()) {
                    image.delete()
                }
            }
            stopLoading()
        }
    }
}