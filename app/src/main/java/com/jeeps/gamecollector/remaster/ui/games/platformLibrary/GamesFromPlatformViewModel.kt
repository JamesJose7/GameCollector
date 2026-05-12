package com.jeeps.gamecollector.remaster.ui.games.platformLibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.GamesRepository
import com.jeeps.gamecollector.remaster.navigation.Screen
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.FilterControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.FilterStats
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.ShowInfoControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.SortControls
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.defaultToAlphabetical
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getAppropriateComparator
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.getFilterData
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs.isNotCleared
import com.jeeps.gamecollector.remaster.utils.extensions.combine
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
import com.jeeps.gamecollector.remaster.utils.extensions.value
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesFromPlatformViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gamesRepository: GamesRepository,
    private val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

    data class GamesFromPlatformUiState(
        val platformId: String = "",
        val platformName: String = "",
        val games: List<Game> = emptyList(),
        val filteredStats: FilterStats = FilterStats(),
        val sortStat: SortStat = SortStat.NONE,
        val searchQuery: String = "",
        val filterControls: FilterControls = FilterControls(),
        val sortControls: SortControls = SortControls(),
        val showInfoControls: ShowInfoControls = ShowInfoControls(),
        val isLoading: Boolean = true
    )

    private val route = savedStateHandle.toRoute<Screen.GamesFromPlatform>()

    private val _dbGames = MutableStateFlow<List<Game>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _filterControls = MutableStateFlow(FilterControls())
    private val _sortControls = MutableStateFlow(SortControls())
    private val _showInfoControls = MutableStateFlow(ShowInfoControls())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<GamesFromPlatformUiState> = combine(
        _dbGames,
        _searchQuery,
        _filterControls,
        _sortControls,
        _showInfoControls,
        _isLoading
    ) { dbGames, query, filters, sortControls, showInfo, loading ->

        val (comparator, sort) = sortControls.getAppropriateComparator()
        val filteredGames = filterAndSortGames(dbGames, query, filters, comparator)

        val totalAmount = dbGames.size
        val filteredAmount = filteredGames.size
        val stats = if (filters.isNotCleared().value()) {
            FilterStats(showStats = true, filteredAmount = filteredAmount, totalAmount = totalAmount)
        } else {
            FilterStats()
        }

        GamesFromPlatformUiState(
            platformId = route.platformId,
            platformName = route.platformName,
            games = filteredGames,
            filteredStats = stats,
            sortStat = sort,
            searchQuery = query,
            filterControls = filters,
            sortControls = sortControls.defaultToAlphabetical(),
            showInfoControls = showInfo,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamesFromPlatformUiState(
            platformId = route.platformId,
            platformName = route.platformName
        )
    )

    init {
        getUserGames()
    }

    private fun getUserGames() {
        val user = authenticationRepository.getUser() ?: return

        viewModelScope.launch {
            gamesRepository.getUserGamesByPlatform(
                user.username,
                route.platformId
            ).collect { state ->
                when (state) {
                    is State.Loading -> _isLoading.value = true
                    is State.Success -> {
                        _isLoading.value = false
                        state.data.let { _dbGames.value = it }
                    }
                    is State.Failed -> {
                        _isLoading.value = false
                        handleError(ErrorType.SERVER_ERROR, state.e)
                    }
                }
            }
        }
    }

    private fun filterAndSortGames(
        dbGames: List<Game>,
        query: String,
        filters: FilterControls,
        comparator: Comparator<Game>
    ): List<Game> {
        val filtersList = filters.getFilterData().filtersList
        return dbGames
            .filter { game -> isGameNameSimilar(game, query) }
            .filter { game -> filtersList.all { it(game) } }
            .sortedWith(comparator)
    }

    fun handleSearch(query: String) {
        _searchQuery.value = query
    }

    fun setFilterControls(filterControls: FilterControls) {
        _filterControls.value = filterControls
    }

    fun setSortControls(sortControls: SortControls) {
        _sortControls.value = sortControls
    }

    fun setShowInfoControls(showInfoControls: ShowInfoControls) {
        _showInfoControls.value = showInfoControls
    }

    fun clearFilters(resetGamesList: Boolean = false) {
        _filterControls.value = FilterControls()
    }

    fun clearShowInfoControls() {
        _showInfoControls.update {
            it.copy(
                isHoursMain = false,
                isHoursExtra = false,
                isHoursCompletionist = false
            )
        }
    }

    // TODO: Replace this with deleting game permanently and restoring it by saving it again
    fun removeGameLocally(game: Game) {
        _dbGames.value = _dbGames.value.filter { it.id != game.id }
    }

    fun addGameLocally(game: Game) {
        _dbGames.value = _dbGames.value + game
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            handleNetworkResponse(gamesRepository.deleteGame(game.id)) {
                postServerMessage("Game deleted successfully")
            }
        }
    }

    private fun isGameNameSimilar(game: Game, query: String): Boolean {
        if (query.isEmpty()) return true
        val name = game.name.lowercase()
        val shortName = game.shortName.lowercase()
        val queryNormalized = query.lowercase()
        return name.contains(queryNormalized) || shortName.contains(queryNormalized)
    }
}
