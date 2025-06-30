package com.jeeps.gamecollector.remaster.ui.games.platformLibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.*
import com.jeeps.gamecollector.remaster.utils.comparators.GameByNameComparator
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.remaster.utils.extensions.value
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _currentFilterControls: MutableStateFlow<FilterControls> = MutableStateFlow(FilterControls())
    val currentFilterControls: StateFlow<FilterControls> = _currentFilterControls.asStateFlow()
    private var _currentSortControls: MutableStateFlow<SortControls> = MutableStateFlow(SortControls())
    val currentSortControls: StateFlow<SortControls> = _currentSortControls.asStateFlow()
    private var _currentShowInfoControls: MutableStateFlow<ShowInfoControls> = MutableStateFlow(ShowInfoControls())
    val currentShowInfoControls: StateFlow<ShowInfoControls> = _currentShowInfoControls.asStateFlow()

    var gamePendingDeletion: Game? = null

    private var dbGames = MutableLiveData<List<Game>>()
    private var currentOrder: Comparator<Game> = GameByNameComparator()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredStats = MediatorLiveData<FilterStats>()
    val filteredStats: MediatorLiveData<FilterStats>
        get() = _filteredStats

    private val _currentSortStat = MutableLiveData(SortStat.NONE)
    val currentSortStat: LiveData<SortStat>
        get() = _currentSortStat

    private val _games = MediatorLiveData<List<Game>>()
    val games: LiveData<List<Game>>
        get() = _games

    init {
        _games.addSource(dbGames) { result ->
            result?.let { game ->
                _games.value = sortGames(game, currentOrder)
                val query = searchQuery.value.takeIf { it.isNotEmpty() }
                query?.let { handleSearch(query) }
                currentFilterControls.value.let { filters ->
                    if (filters.isNotCleared()) {
                        updateFilters(filters.getFilterData().filtersList)
                    }
                }
            }
        }

        _filteredStats.addSource(games) { result ->
            result?.let { games ->
                _filteredStats.value = if (currentFilterControls.value?.isNotCleared().value()) {
                    val totalAmount = dbGames.value?.size ?: 0
                    val filteredAmount = if (totalAmount == 0) 0 else games.size
                    FilterStats(true, filteredAmount, totalAmount)
                } else {
                    FilterStats()
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

    private fun filterGames(
        unfilteredGames: List<Game>,
        filtersList: List<(Game) -> Boolean>
    ): List<Game> {
        val filteredGames = mutableListOf<Game>()
        filteredGames.addAll(
            unfilteredGames.filter { game ->
                filtersList.all { filter ->
                    filter(game)
                }
            }
        )
        return if (filtersList.isEmpty())
            unfilteredGames.sortedWith(currentOrder)
        else
            filteredGames.sortedWith(currentOrder)
    }

    fun setCurrentSortStat(sortStat: SortStat) {
        _currentSortStat.value = sortStat
    }

    fun setFilterControls(filterControls: FilterControls) {
        _currentFilterControls.value = filterControls
    }

    fun setSortControls(sortControls: SortControls) {
        _currentSortControls.value = sortControls
    }

    fun setShowInfoControls(showInfoControls: ShowInfoControls) {
        _currentShowInfoControls.value = showInfoControls
    }

    fun rearrangeGames(comparator: Comparator<Game>) = dbGames.value?.let {
        _games.value = sortGames(it, comparator)
    }.also {
        currentOrder = comparator
        if (searchQuery.value.isNotEmpty()) handleSearch(searchQuery.value)
        currentFilterControls.value.let { filters ->
            if (filters.isNotCleared()) {
                updateFilters(filters.getFilterData().filtersList)
            }
        }
    }

    fun updateFilters(filtersList: List<(Game) -> Boolean>) = dbGames.value?.let {
        _games.value = filterGames(it, filtersList)
    }

    fun clearFilters(resetGamesList: Boolean = false) {
        _currentFilterControls.value = FilterControls()
        if (resetGamesList) {
            updateFilters(listOf())
        }
    }

    fun getGameAt(position: Int): Game? {
        return games.value?.get(position)
    }

    // TODO: Replace this with deleting game permanently and restoring it by saving it again
    fun removeGameLocally(game: Game) {
        _games.value = _games.value
            ?.filter { it.id != game.id }
            ?.sortedWith(currentOrder)
    }

    fun addGameLocally(game: Game) {
        _games.value = _games.value
            ?.plus(listOf(game))
            ?.sortedWith(currentOrder)
    }

    fun deleteGame(game: Game) {
        gamePendingDeletion = null

        viewModelScope.launch {
            val token = authenticationRepository.getUserToken()

            handleNetworkResponse(gamesRepository.deleteGame(token, game.id)) {
                postServerMessage("Game deleted successfully")
            }
        }
    }

    fun deleteGamePendingDeletion() {
        gamePendingDeletion?.let { game ->
            deleteGame(game)
        }
    }

    fun handleSearch(query: String) {
        _searchQuery.value = query
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

    fun clearShowInfoControls() {
        _currentShowInfoControls.update {
            it.copy(
                isHoursMain = false,
                isHoursExtra = false,
                isHoursCompletionist = false
            )
        }
    }
}