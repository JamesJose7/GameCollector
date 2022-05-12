package com.jeeps.gamecollector.remaster.ui.games.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.data.repository.IgdbRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.utils.Event
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.remaster.utils.extensions.similarity
import com.jeeps.gamecollector.utils.IgdbUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val igdbRepository: IgdbRepository
) : BaseViewModel() {

    private var timesCompleted: Int = 0

    private val _selectedGame = MutableLiveData<Game>()
    val selectedGame: LiveData<Game>
        get() = _selectedGame

    private val _isImageReadyToUpload = MutableLiveData<Event<Boolean>>()
    val isImageReadyToUpload: LiveData<Event<Boolean>>
        get() = _isImageReadyToUpload

    var selectedGamePosition: Int = -1
    var platformName: String? = null
    var platformId: String? = null

    var currentImageUri: Uri? = null
    var coverDeleted: Boolean = false

    private var pendingMessage: String = ""

    fun setSelectedGame(game: Game) {
        _selectedGame.value = game
    }

    fun setTimesCompleted(value: Int) {
        timesCompleted = value
        _selectedGame.value?.timesCompleted = value
    }

    fun setGameFormat(isPhysical: Boolean) {
        _selectedGame.value?.isPhysical = isPhysical
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

    fun saveGame() {
        selectedGame.value?.let { game ->
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
            when (val response = gamesRepository.saveNewGame(token, game)) {
                is NetworkResponse.Success -> {
                    if (currentImageUri != null) {
                        setSelectedGame(response.body)
                        pendingMessage = "Game created successfully"
                        _isImageReadyToUpload.value = Event(true)
                    } else {
                        postServerMessage("Game created successfully")
                        stopLoading()
                    }
                }
                is NetworkResponse.Error -> {
                    handleError(response)
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
                    _isImageReadyToUpload.value = Event(true)
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

            val selectedGame = if (igdbGames == null || igdbGames.isEmpty()) {
                null
            } else {
                // Exclude DLC and sort based on most similar name based on the user input
                val sortedGames = igdbGames
                    .sortedByDescending { igGame ->
                        igGame.name.similarity(
                            _selectedGame.value?.name ?: ""
                        )
                    }

                sortedGames
                    .firstOrNull { igGame -> igGame.category != 1 }
            }

            if (selectedGame == null) {
                continueSavingGame(isEdit, game)
            } else {
                // Get image cover
                when (val response = igdbRepository
                    .getGameCoverById(IgdbUtils.getCoverImageQuery(selectedGame.cover))) {
                    is NetworkResponse.Success -> {
                        val gameCovers = response.body
                        if (gameCovers.isNotEmpty()) {
                            gameCovers[0].getBigCoverUrl().let { coverUrl ->
                                _selectedGame.value?.imageUri = coverUrl
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

    fun uploadCoverImage(imageFile: File?) {
        viewModelScope.launch {
            startLoading()
            imageFile?.let { image ->
                val token = authenticationRepository.getUserToken()
                val requestFile = image
                    .asRequestBody("image/png".toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("image", image.name, requestFile)

                handleNetworkResponse(gamesRepository
                    .uploadGameCover(token, selectedGame.value?.id ?: "", body)) {
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