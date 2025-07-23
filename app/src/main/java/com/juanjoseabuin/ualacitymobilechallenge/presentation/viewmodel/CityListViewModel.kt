package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CityListViewModel @Inject constructor(
    private val repository: CityRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        CityListState(
            isLoading = true,
            searchQuery = "",
            selectedTabIndex = savedStateHandle[SELECTED_TAB_INDEX_KEY] ?: ALL_CITIES_TAB_INDEX
        )
    )
    val state: StateFlow<CityListState> = _state.asStateFlow()

    // NEW: Refresh trigger for Favorite Cities tab - THIS WAS MISSING DECLARATION
    private val _favoriteCitiesRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)


    init {
        val searchQueryFlow = _state.map { it.searchQuery }
            .debounce(300.milliseconds)
            .distinctUntilChanged()

        // --- Data collection flow for ALL CITIES tab ---
        combine(
            searchQueryFlow,
            _state.map { it.allCitiesCurrentPage }.distinctUntilChanged()
        ) { searchQuery, currentPage ->
            val offset = currentPage * PAGE_SIZE
            Log.d(TAG, "Fetching All Cities Page: $currentPage, Query: '$searchQuery', Offset: $offset")
            repository.getPaginatedCities(
                limit = PAGE_SIZE,
                offset = offset,
                onlyFavorites = false,
                searchQuery = searchQuery
            )
        }
            .flatMapLatest { it }
            .onStart {
                if (_state.value.allCities.isEmpty() && _state.value.allCitiesCurrentPage == 0) {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
            }
            .onEach { newCitiesFromDb -> // Renamed for clarity: data directly from DB flow
                _state.update { currentState ->
                    val updatedList = if (currentState.allCitiesCurrentPage == 0) {
                        // For the first page, always replace entirely.
                        newCitiesFromDb.map { it.toUiItem() }
                    } else {
                        currentState.allCities + newCitiesFromDb.map { it.toUiItem() }
                    }
                    currentState.copy(
                        allCities = updatedList,
                        hasMoreAllCities = newCitiesFromDb.size == PAGE_SIZE,
                        isLoading = false,
                        isLoadingAllCitiesNextPage = false,
                        error = null
                    )
                }
                Log.d(TAG, "All Cities: Emitted ${newCitiesFromDb.size} cities. Total: ${_state.value.allCities.size}. HasMore: ${_state.value.hasMoreAllCities}")
            }
            .catch { e ->
                Log.e(TAG, "Error fetching all cities: ${e.message}", e)
                _state.update {
                    it.copy(
                        error = "Error loading all cities: ${e.message}",
                        isLoading = false,
                        isLoadingAllCitiesNextPage = false
                    )
                }
            }
            .launchIn(viewModelScope)


        // --- Data collection flow for FAVORITE CITIES tab ---
        combine(
            searchQueryFlow,
            _state.map { it.favoriteCitiesCurrentPage }.distinctUntilChanged(),
            _favoriteCitiesRefreshTrigger.onStart { emit(Unit) } // Include refresh trigger
        ) { searchQuery, currentPage, _ -> // `_` is Unit, ignored
            val offset = currentPage * PAGE_SIZE
            Log.d(TAG, "Fetching Favorite Cities Page: $currentPage, Query: '$searchQuery', Offset: $offset (Triggered by refresh or page/query change)")
            repository.getPaginatedCities(
                limit = PAGE_SIZE,
                offset = offset,
                onlyFavorites = true,
                searchQuery = searchQuery
            )
        }
            .flatMapLatest { it }
            .onStart {
                if (_state.value.favoriteCities.isEmpty() && _state.value.favoriteCitiesCurrentPage == 0 && _state.value.selectedTabIndex == FAVORITE_CITIES_TAB_INDEX) {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
            }
            .onEach { newCities ->
                _state.update { currentState ->
                    val updatedList = if (currentState.favoriteCitiesCurrentPage == 0) {
                        newCities.map { it.toUiItem() } // Replace for first page/new search/refresh
                    } else {
                        currentState.favoriteCities + newCities.map { it.toUiItem() }
                    }
                    currentState.copy(
                        favoriteCities = updatedList,
                        hasMoreFavoriteCities = newCities.size == PAGE_SIZE,
                        isLoading = false, // Initial loading finished
                        isLoadingFavoriteCitiesNextPage = false,
                        error = null // Clear any errors
                    )
                }
                Log.d(TAG, "Favorite Cities: Emitted ${newCities.size} cities. Total: ${_state.value.favoriteCities.size}. HasMore: ${_state.value.hasMoreFavoriteCities}")
            }
            .catch { e ->
                Log.e(TAG, "Error fetching favorite cities: ${e.message}", e)
                _state.update {
                    it.copy(
                        error = "Error loading favorite cities: ${e.message}",
                        isLoadingFavoriteCitiesNextPage = false,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)

        Log.i(TAG, "ViewModel init complete. Observing for state changes.")
    }

    /**
     * Central function to handle all incoming UI actions (Intents).
     */
    fun onAction(action: CityListAction) {
        when (action) {
            is CityListAction.OnSearchQueryChange -> {
                _state.update {
                    it.copy(
                        searchQuery = action.query,
                        allCitiesCurrentPage = 0,
                        hasMoreAllCities = true,
                        isLoadingAllCitiesNextPage = false,
                        favoriteCitiesCurrentPage = 0,
                        hasMoreFavoriteCities = true,
                        isLoadingFavoriteCitiesNextPage = false
                    )
                }
            }
            is CityListAction.OnTabSelected -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedTabIndex = action.tabIndex,
                        searchQuery = "",
                        allCitiesCurrentPage = 0,
                        hasMoreAllCities = true,
                        isLoadingAllCitiesNextPage = false,
                        favoriteCitiesCurrentPage = 0,
                        hasMoreFavoriteCities = true,
                        isLoadingFavoriteCitiesNextPage = false
                    )
                }
                savedStateHandle[SELECTED_TAB_INDEX_KEY] = action.tabIndex

                if (action.tabIndex == FAVORITE_CITIES_TAB_INDEX) {
                    viewModelScope.launch {
                        _favoriteCitiesRefreshTrigger.emit(Unit)
                    }
                }
            }
            is CityListAction.OnToggleCityFavoriteStatus -> {
                toggleCityFavoriteStatus(action.cityId)
            }
            is CityListAction.OnLoadNextPage -> {
                loadNextPage()
            }
            is CityListAction.OnCityClick -> Unit
            is CityListAction.OnCityDetailsClick -> Unit
        }
    }

    /**
     * Toggles the favorite status of a city by its ID.
     * This function now includes optimistic UI updates for the "All Cities" tab
     * and forces a full refresh for the "Favorite Cities" tab.
     * @param cityId The ID of the city to toggle.
     */
    private fun toggleCityFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "ViewModel: Toggling favorite status for city ID: $cityId")

            // Determine current status BEFORE any state updates, so it's available in catch block
            val currentCityInAllCities = _state.value.allCities.find { it.id == cityId }
            val currentCityInFavorites = _state.value.favoriteCities.find { it.id == cityId }

            val currentFavoriteStatus = currentCityInAllCities?.isFavorite
                ?: currentCityInFavorites?.isFavorite
                ?: false // Default to false if not found (shouldn't happen for a toggled city)

            val newFavoriteStatus = !currentFavoriteStatus
            Log.d(
                TAG,
                "City ID: $cityId, Current Fav: $currentFavoriteStatus, New Fav: $newFavoriteStatus"
            )

            try {
                // --- Optimistic UI update ---
                _state.update { currentState ->
                    //  Always update allCities list optimistically
                    val updatedAllCities = currentState.allCities.map { city ->
                        if (city.id == cityId) {
                            Log.d(
                                TAG,
                                "All Cities: Optimistically updating UI for city ${city.name} (ID: $cityId) status to ${newFavoriteStatus}."
                            )
                            city.copy(isFavorite = newFavoriteStatus)
                        } else {
                            city
                        }
                    }

                    val updatedFavoriteCities =
                        if (currentState.selectedTabIndex == FAVORITE_CITIES_TAB_INDEX) {
                            if (!newFavoriteStatus) {
                                Log.d(
                                    TAG,
                                    "Favorite Cities: Optimistically removing city ID: $cityId (toggled to unfavorite)."
                                )
                                currentState.favoriteCities.filter { it.id != cityId }
                            } else {
                                currentState.favoriteCities.map { city ->
                                    if (city.id == cityId) city.copy(isFavorite = newFavoriteStatus) else city
                                }
                            }
                        } else {
                            currentState.favoriteCities
                        }

                    currentState.copy(
                        allCities = updatedAllCities,
                        favoriteCities = updatedFavoriteCities,
                        isLoading = false,
                        error = null
                    )
                }

                // Perform the actual database update asynchronously
                repository.toggleCityFavoriteStatusById(cityId)
                Log.i(TAG, "ViewModel: Successfully initiated DB toggle for city ID: $cityId.")

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "ViewModel: Error toggling favorite status for city ID $cityId: ${e.message}",
                    e
                )
                // Revert optimistic UI update if there's an error and the operation failed
                _state.update { currentState ->

                    val revertedAllCities = currentState.allCities.map { city ->
                        if (city.id == cityId) city.copy(isFavorite = currentFavoriteStatus) else city
                    }

                    // ONLY revert 'favoriteCities' if the Favorites tab was active during the attempt
                    val revertedFavoriteCities =
                        if (currentState.selectedTabIndex == FAVORITE_CITIES_TAB_INDEX) {
                            if (!newFavoriteStatus) {
                                val cityToRevert =
                                    currentCityInAllCities?.copy(isFavorite = currentFavoriteStatus)
                                        ?: currentCityInFavorites?.copy(isFavorite = currentFavoriteStatus)
                                        ?: CityUiItem(
                                            id = cityId,
                                            name = "Unknown City",
                                            country = "Unknown",
                                            isFavorite = currentFavoriteStatus,
                                            fullName = "Unknown City, Unknown"
                                        )

                                if (!currentState.favoriteCities.any { it.id == cityId }) {
                                    Log.d(
                                        TAG,
                                        "Favorite Cities: Reverting failed unfavorite, adding city ID: $cityId back."
                                    )
                                    (currentState.favoriteCities + cityToRevert).sortedBy { it.fullName }
                                } else {
                                    currentState.favoriteCities.map { city ->
                                        if (city.id == cityId) city.copy(isFavorite = currentFavoriteStatus) else city
                                    }.sortedBy { it.fullName }
                                }
                            } else {
                                currentState.favoriteCities.map { city ->
                                    if (city.id == cityId) city.copy(isFavorite = currentFavoriteStatus) else city
                                }
                            }
                        } else {
                            currentState.favoriteCities
                        }

                    currentState.copy(
                        allCities = revertedAllCities,
                        favoriteCities = revertedFavoriteCities,
                        error = "Failed to toggle favorite status for ID $cityId: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Triggers the loading of the next page for the currently selected tab.
     */
    private fun loadNextPage() {
        _state.update { currentState ->
            when (currentState.selectedTabIndex) {
                ALL_CITIES_TAB_INDEX -> {
                    if (currentState.hasMoreAllCities && !currentState.isLoadingAllCitiesNextPage) {
                        Log.d(TAG, "Loading next page for All Cities: ${currentState.allCitiesCurrentPage + 1}")
                        currentState.copy(
                            allCitiesCurrentPage = currentState.allCitiesCurrentPage + 1,
                            isLoadingAllCitiesNextPage = true
                        )
                    } else {
                        currentState // No more pages or already loading
                    }
                }
                FAVORITE_CITIES_TAB_INDEX -> {
                    if (currentState.hasMoreFavoriteCities && !currentState.isLoadingFavoriteCitiesNextPage) {
                        Log.d(TAG, "Loading next page for Favorite Cities: ${currentState.favoriteCitiesCurrentPage + 1}")
                        currentState.copy(
                            favoriteCitiesCurrentPage = currentState.favoriteCitiesCurrentPage + 1,
                            isLoadingFavoriteCitiesNextPage = true
                        )
                    } else {
                        currentState // No more pages or already loading
                    }
                }
                else -> currentState
            }
        }
    }

    companion object {
        private const val SELECTED_TAB_INDEX_KEY = "selected_tab_index"
        const val ALL_CITIES_TAB_INDEX = 0
        const val FAVORITE_CITIES_TAB_INDEX = 1
        private const val TAG = "CityListViewModel"

        private const val PAGE_SIZE = 20 // Define your page size here
    }
}

sealed class CityListAction {
    data class OnSearchQueryChange(val query: String) : CityListAction()
    data class OnTabSelected(val tabIndex: Int) : CityListAction() // New action for tab selection
    data class OnToggleCityFavoriteStatus(val cityId: Long) : CityListAction()
    data object OnLoadNextPage : CityListAction()
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
    val selectedTabIndex: Int = CityListViewModel.ALL_CITIES_TAB_INDEX,

    // Pagination state for ALL_CITIES tab
    val allCitiesCurrentPage: Int = 0,
    val hasMoreAllCities: Boolean = true, // True if there might be more pages to load
    val isLoadingAllCitiesNextPage: Boolean = false, // True if currently loading a subsequent page

    // Pagination state for FAVORITE_CITIES tab
    val favoriteCitiesCurrentPage: Int = 0,
    val hasMoreFavoriteCities: Boolean = true,
    val isLoadingFavoriteCitiesNextPage: Boolean = false,
)