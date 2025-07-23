package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.domain.utils.SearchCityUtils.searchByPrefix
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel

class CityListViewModel @Inject constructor(
    private val repository: CityRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Internal mutable StateFlow, represents the single source of truth for UI state
    private val _state = MutableStateFlow(
        CityListState(
            isLoading = true, // Initial loading state
            searchQuery = "",
        )
    )
    val state: StateFlow<CityListState> = _state.asStateFlow() // Expose as immutable StateFlow

    // Optimistic favorite changes: Map of cityId to its *optimistic* isFavorite status
    private val _optimisticFavoriteChanges = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    // Job to cancel previous search/filter requests (good practice for `flatMapLatest`)
    private var dataCollectionJob: Job? = null

    init {
        // Centralized data observation and processing
        dataCollectionJob = combine(
            repository.getCities(), // All cities from DB
            repository.getFavoriteCities(), // All favorite cities from DB
            _state.map { it.searchQuery }.debounce(300.milliseconds).distinctUntilChanged(), // Debounced search query
            _optimisticFavoriteChanges // Optimistic updates
        ) { allCities, favoriteCities, searchQuery, optimisticChanges ->

            // 1. Apply optimistic updates to both lists
            val allCitiesWithOptimistic = allCities.map { city ->
                optimisticChanges[city.id]?.let { isFavoriteOptimistic ->
                    city.copy(isFavorite = isFavoriteOptimistic)
                } ?: city
            }

            val favoriteCitiesWithOptimistic = favoriteCities.map { city ->
                optimisticChanges[city.id]?.let { isFavoriteOptimistic ->
                    city.copy(isFavorite = isFavoriteOptimistic)
                } ?: city
            }

            // 2. Apply search filter to both lists
            val searchedAllCities = if (searchQuery.isBlank()) {
                allCitiesWithOptimistic
            } else {
                allCitiesWithOptimistic.searchByPrefix(searchQuery)
            }

            val searchedFavoriteCities = if (searchQuery.isBlank()) {
                favoriteCitiesWithOptimistic
            } else {
                favoriteCitiesWithOptimistic.searchByPrefix(searchQuery)
            }.filter { it.isFavorite } // Ensure only actual favorites (or optimistically favorited) are in this list

            Pair(searchedAllCities, searchedFavoriteCities)
        }
            .onStart {
                // Indicate loading when the main data collection flow starts
                _state.update { it.copy(isLoading = true, error = null) }
            }
            .onEach { (searchedAllCities, searchedFavoriteCities) ->
                _state.update { currentState ->
                    currentState.copy(
                        allCities = searchedAllCities.map { it.toUiItem() },
                        favoriteCities = searchedFavoriteCities.map { it.toUiItem() },
                        isLoading = false,
                        error = null,
                    )
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in data collection: ${e.message}", e)
                _state.update {
                    it.copy(
                        error = "Error loading cities: ${e.message}",
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope) // Launch the main data collection flow

        Log.i(TAG, "ViewModel init complete. Observing for state changes.")
    }

    /**
     * Central function to handle all incoming UI actions (Intents).
     */
    fun onAction(action: CityListAction) {
        when (action) {
            is CityListAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
            }
            is CityListAction.OnTabSelected -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedTabIndex = action.tabIndex,
                        searchQuery = "" // Clear search when switching tabs for a clean state
                    )
                }
                savedStateHandle[SELECTED_TAB_INDEX_KEY] = action.tabIndex
            }
            is CityListAction.OnToggleCityFavoriteStatus -> {
                toggleCityFavoriteStatus(action.cityId)
            }

            is CityListAction.OnCityClick -> Unit
            is CityListAction.OnCityDetailsClick -> Unit
        }
    }

    /**
     * Toggles the favorite status of a city by its ID.
     * Includes optimistic updates to the UI.
     * @param cityId The ID of the city to toggle.
     */
    private fun toggleCityFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "ViewModel: Toggling favorite status for city ID: $cityId")

            // Determine current actual status from the main 'allCities' list in state (or repository if more robust check needed)
            val currentActualStatus = _state.value.allCities.firstOrNull { it.id == cityId }?.isFavorite
                ?: false // Default to false if not found

            val newOptimisticStatus = !currentActualStatus

            // Apply optimistic update
            _optimisticFavoriteChanges.update { currentMap ->
                currentMap + (cityId to newOptimisticStatus)
            }
            _state.update { it.copy(togglingCityIds = it.togglingCityIds + cityId) }

            try {
                repository.toggleCityFavoriteStatusById(cityId)
                Log.i(TAG, "ViewModel: Successfully toggled favorite status for city ID: $cityId")
                // On success, clear the optimistic change for this item
                _optimisticFavoriteChanges.update { currentMap ->
                    currentMap - cityId
                }
            } catch (e: Exception) {
                Log.e(TAG, "ViewModel: Error toggling favorite status for city ID $cityId: ${e.message}", e)
                // On error, revert the optimistic change
                _optimisticFavoriteChanges.update { currentMap ->
                    currentMap + (cityId to currentActualStatus) // Revert to original status
                }
                _state.update { it.copy(error = "Failed to toggle favorite status for ID $cityId: ${e.message}") }
            } finally {
                _state.update { it.copy(togglingCityIds = it.togglingCityIds - cityId) }
            }
        }
    }

    companion object {
        private const val SELECTED_TAB_INDEX_KEY = "selected_tab_index" // New key for saved state
        const val ALL_CITIES_TAB_INDEX = 0
        const val FAVORITE_CITIES_TAB_INDEX = 1
        private const val TAG = "CityListViewModel"
    }
}

sealed class CityListAction {
    data class OnSearchQueryChange(val query: String) : CityListAction()
    data class OnTabSelected(val tabIndex: Int) : CityListAction() // New action for tab selection
    data class OnToggleCityFavoriteStatus(val cityId: Long) : CityListAction()
    data class OnCityClick(val city: CityUiItem) : CityListAction()
    data class OnCityDetailsClick(val city: CityUiItem) : CityListAction()
}

data class CityListState(
    val allCities: List<CityUiItem> = emptyList(), // Store all cities (after search, before favorite filter)
    val favoriteCities: List<CityUiItem> = emptyList(), // Store only favorite cities (after search)
    val searchQuery: String = "",
    val isLoading: Boolean = false, // True for initial or full data load
    val error: String? = null,
    val togglingCityIds: Set<Long> = emptySet(), // Set of IDs currently being toggled
    val selectedTabIndex: Int = CityListViewModel.ALL_CITIES_TAB_INDEX
)