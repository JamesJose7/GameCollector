package com.jeeps.gamecollector.remaster.ui.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.comparators.GameByNameComparator
import com.jeeps.gamecollector.model.Game
import com.jeeps.gamecollector.model.SortStat
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class GamesFromPlatformViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

    var platformId: String = ""
        set(value) {
            field = value
            getUserGames()
        }

    var platformName: String = ""

    private var currentOrder: Comparator<Game> = GameByNameComparator()
    private var currentQuery: String = ""

    private val _currentSortStat = MutableLiveData(SortStat.NONE)
    val currentSortStat: LiveData<SortStat>
        get() = _currentSortStat

    var gamePendingDeletion: Game? = null

    private var dbGames = MutableLiveData<List<Game>>()

    private val _games = MediatorLiveData<List<Game>>()
    val games: LiveData<List<Game>>
        get() = _games

    init {
        _games.addSource(dbGames) { result ->
            result?.let { game ->
                _games.value = sortGames(game, currentOrder)
                val query = currentQuery.takeIf { it.isNotEmpty() }
                query?.let { handleSearch(query) }
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
                            result?.let { dbGames.value = it }
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

    private fun sortGames(
        unsortedGames: List<Game>,
        currentOrder: Comparator<Game>
    ): List<Game> {
        return unsortedGames.sortedWith(currentOrder)
    }

    fun setCurrentSortState(sortStat: SortStat) {
        _currentSortStat.value = sortStat
    }

    fun rearrangeGames(comparator: Comparator<Game>) = dbGames.value?.let {
        _games.value = sortGames(it, comparator)
    }.also {
        currentOrder = comparator
        if (currentQuery.isNotEmpty()) handleSearch(currentQuery)
    }

    fun getGameAt(position: Int): Game? {
        return games.value?.get(position)
    }

    fun deleteGame(game: Game) {
        gamePendingDeletion = null
        viewModelScope.launch {
            val token = authenticationRepository.getUserToken()
            game.id?.let { gameId ->
                when (val response = gamesRepository.deleteGame(token, gameId)) {
                    is NetworkResponse.Success -> {
                        postServerMessage("Game deleted successfully")
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

    fun deleteGamePendingDeletion() {
        gamePendingDeletion?.let { game ->
            deleteGame(game)
        }
    }

    fun handleSearch(query: String) {
        currentQuery = query
        dbGames.value
            ?.sortedWith(currentOrder)
            ?.filter { game -> isGameNameSimilar(game, query) }
            .also { games ->
                games?.let {
                    _games.value = it
                }
            }
    }

    private fun isGameNameSimilar(game: Game, query: String): Boolean {
        val name = game.name.lowercase()
        val shortName = game.shortName.lowercase()
        val queryNormalized = query.lowercase()
        return name.contains(queryNormalized) || shortName.contains(queryNormalized)
    }
}