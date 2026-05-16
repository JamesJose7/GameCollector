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
        val genresFilterControls: List<GenreFilter> = emptyList(),
        val isLoading: Boolean = true
    )

    data class GenreFilter(
        val name: String,
        val enabled: Boolean
    )

    private val route = savedStateHandle.toRoute<Screen.GamesFromPlatform>()

    private val _dbGames = MutableStateFlow<List<Game>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _filterControls = MutableStateFlow(FilterControls())
    private val _sortControls = MutableStateFlow(SortControls())
    private val _showInfoControls = MutableStateFlow(ShowInfoControls())
    private val _genresFilterControls = MutableStateFlow<List<GenreFilter>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<GamesFromPlatformUiState> = combine(
        _dbGames,
        _searchQuery,
        _filterControls,
        _sortControls,
        _showInfoControls,
        _genresFilterControls,
        _isLoading
    ) { dbGames, query, filters, sortControls, showInfo, genresFilters, loading ->

        val (comparator, sort) = sortControls.getAppropriateComparator()
        val enabledGenresFilters = genresFilters.filter { it.enabled }.map { it.name }
        val filteredGames = filterAndSortGames(dbGames, query, filters, enabledGenresFilters, comparator)

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
            genresFilterControls = genresFilters,
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
                        state.data.let {
                            _dbGames.value = it
                            if (uiState.value.genresFilterControls.isEmpty()) {
                                populateGenres(it)
                            }
                        }
                    }
                    is State.Failed -> {
                        _isLoading.value = false
                        handleError(ErrorType.SERVER_ERROR, state.e)
                    }
                }
            }
        }
    }

    private fun populateGenres(games: MutableList<Game>) {
        val genres = games
            .flatMap { it.genresNames }
            .distinct()
            .sorted()
            .map { GenreFilter(it, false) }

        _genresFilterControls.value = genres
    }

    private fun filterAndSortGames(
        dbGames: List<Game>,
        query: String,
        filters: FilterControls,
        genresFilters: List<String>,
        comparator: Comparator<Game>
    ): List<Game> {
        val filtersList = filters.getFilterData().filtersList
        return dbGames
            .filter { game -> queryGame(game, query) }
            .filter { game -> filtersList.all { it(game) } }
            .filter { game -> filterGenres(game, genresFilters) }
            .sortedWith(comparator)
    }

    private fun filterGenres(game: Game, genresFilters: List<String>): Boolean {
        if (genresFilters.isEmpty()) return true
        return game.genresNames.any { it in genresFilters }
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

    fun clearFiltersAndSort() {
        setFilterControls(FilterControls())
        setSortControls(SortControls())
        clearShowInfoControls()
        clearGenreFilters()
    }

    fun clearShowInfoControls() {
        setShowInfoControls(ShowInfoControls())
    }

    fun clearGenreFilters() {
        _genresFilterControls.value = _genresFilterControls.value.map { it.copy(enabled = false) }
    }

    fun updateGenreFilters(genreFilter: GenreFilter) {
        _genresFilterControls.value = _genresFilterControls.value
            .map { if (it.name == genreFilter.name) it.copy(enabled = genreFilter.enabled) else it }
    }

    // TODO: Replace this with deleting game permanently and restoring it by saving it again
    fun removeGameLocally(game: Game) {
        _dbGames.value = _dbGames.value.filter { it.id != game.id }
    }

    fun addGameLocally(game: Game) {
        _dbGames.value += game
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            handleNetworkResponse(gamesRepository.deleteGame(game.id)) {
                postServerMessage("Game deleted successfully")
            }
        }
    }

    // Include either game names or publishers
    private fun queryGame(game: Game, query: String): Boolean =
        isGameNameSimilar(game, query) || isPublishedSimilar(game, query)

    private fun isGameNameSimilar(game: Game, query: String): Boolean {
        if (query.isEmpty()) return true
        val name = game.name.lowercase()
        val shortName = game.shortName.lowercase()
        val queryNormalized = query.lowercase()
        return name.contains(queryNormalized) || shortName.contains(queryNormalized)
    }

    private fun isPublishedSimilar(game: Game, query: String): Boolean {
        if (query.isEmpty()) return true
        val publisher = game.publisher.lowercase()
        val queryNormalized = query.lowercase()
        return publisher.contains(queryNormalized)
    }
}
