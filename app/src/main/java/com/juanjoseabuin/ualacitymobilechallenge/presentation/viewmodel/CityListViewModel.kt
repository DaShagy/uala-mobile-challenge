package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class)
class CityListViewModel @Inject constructor(
    private val repository: CityRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CityListUiState(
            isLoading = true,
            searchQuery = savedStateHandle.get<String>(SEARCH_QUERY_KEY) ?: ""
        )
    )
    val uiState: StateFlow<CityListUiState> = _uiState.asStateFlow()

    private val _optimisticFavoriteChanges = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    init {
        // Main flow for all cities, incorporating optimistic updates and search filtering
        repository.getCities()
            .map { cities -> cities.map { it.toUiItem() } }
            .combine(_optimisticFavoriteChanges) { actualCities, optimisticChanges ->
                // Apply optimistic changes on top of actual data from the repository
                actualCities.map { city ->
                    optimisticChanges[city.id]?.let { isFavoriteOptimistic ->
                        city.copy(isFavorite = isFavoriteOptimistic)
                    } ?: city // Use optimistic status if present, else use actual status
                }
            }
            .combine(_uiState.map { it.searchQuery }) { combinedCities, searchQuery ->
                // Then apply search filtering
                if (searchQuery.isBlank()) {
                    combinedCities
                } else {
                    combinedCities.filter { it.name.startsWith(searchQuery, ignoreCase = true) }
                }
            }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .onEach { filteredCities ->
                _uiState.update { currentState ->
                    val noResults = filteredCities.isEmpty() &&
                            currentState.searchQuery.isNotBlank() &&
                            currentState.error == null

                    currentState.copy(
                        displayedCities = filteredCities,
                        noResultsFound = noResults,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        error = "Error loading/filtering cities: ${e.message}",
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)


        // Flow for favorite cities, also incorporating optimistic updates
        repository.getFavoriteCities()
            .map { favorites -> favorites.map { it.toUiItem() } } // Map domain to UI items
            .combine(_optimisticFavoriteChanges) { actualFavorites, optimisticChanges ->
                // Apply optimistic changes. Filter to ensure only actual favorites or optimistically favorited appear.
                actualFavorites.map { fav ->
                    optimisticChanges[fav.id]?.let { isFavoriteOptimistic ->
                        fav.copy(isFavorite = isFavoriteOptimistic)
                    } ?: fav
                }.filter { it.isFavorite } // Ensure only favorites are in this list
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Error fetching favorites: ${e.message}") }
                emit(emptyList())
            }
            .onEach { favorites ->
                _uiState.update { it.copy(favoriteCities = favorites) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    private val VM_TAG = "CityListViewModel" // A specific tag for ViewModel logs

    fun toggleCityFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            Log.d(VM_TAG, "toggleCityFavoriteStatus: [START] for City ID: $cityId")

            // Get the current favorite status from the UI state (which might already be optimistic)
            val currentDisplayedStatus = _uiState.value.displayedCities
                .firstOrNull { it.id == cityId }?.isFavorite
                ?: false

            val newOptimisticStatus = !currentDisplayedStatus
            Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Current displayed status: $currentDisplayedStatus, New optimistic status: $newOptimisticStatus")

            // 1. Immediately apply the optimistic update to our internal map
            _optimisticFavoriteChanges.update { currentMap ->
                val updatedMap = currentMap + (cityId to newOptimisticStatus)
                Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - _optimisticFavoriteChanges updated to: $updatedMap")
                updatedMap
            }

            // 2. Indicate that a toggle operation is in progress (for the sync icon, if used)
            _uiState.update { it.copy(togglingCityIds = it.togglingCityIds + cityId) }
            Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Added to togglingCityIds. Current toggling IDs: ${_uiState.value.togglingCityIds}")

            try {
                // 3. Call the repository to perform the actual database update
                Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Calling repository.toggleCityFavoriteStatusById...")
                repository.toggleCityFavoriteStatusById(cityId) // This is your suspend function call
                Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Repository call SUCCEEDED.")

                // 4. On success: Schedule the removal of the optimistic override after 10 seconds.
                //    This `launch` block creates a *new, non-blocking* coroutine.
                //    It runs concurrently and will not block the `finally` block below.
                launch {
                    Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Starting 10-second optimistic hold delay...")
                    delay(10_000) // This is the actual 10-second pause for the optimistic state
                    Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - 10-second optimistic hold ended. Removing from _optimisticFavoriteChanges.")
                    _optimisticFavoriteChanges.update { currentMap ->
                        val updatedMap = currentMap - cityId
                        Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - _optimisticFavoriteChanges after 10s hold: $updatedMap")
                        updatedMap
                    }
                }

            } catch (e: Exception) {
                // 5. On failure: Revert the optimistic change immediately and set an error.
                Log.e(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Repository call FAILED: ${e.message}", e)
                _optimisticFavoriteChanges.update { currentMap ->
                    val updatedMap = currentMap + (cityId to currentDisplayedStatus) // Revert to original status
                    Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Reverted _optimisticFavoriteChanges on failure: $updatedMap")
                    updatedMap
                }
                _uiState.update { it.copy(error = "Failed to toggle favorite status for ID $cityId: ${e.message}") }
                Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - UI error set: ${_uiState.value.error}")

            } finally {
                // 6. Always remove from toggling IDs, as the network/database operation has now concluded
                //    (whether it succeeded or failed). This will hide the 'sync' icon.
                _uiState.update { it.copy(togglingCityIds = it.togglingCityIds - cityId) }
                Log.d(VM_TAG, "toggleCityFavoriteStatus: City ID: $cityId - Removed from togglingCityIds (finally block). Current toggling IDs: ${_uiState.value.togglingCityIds}")
                Log.d(VM_TAG, "toggleCityFavoriteStatus: [END] for City ID: $cityId. Main coroutine finished.")
            }
        }
    }

    data class CityListUiState(
        val displayedCities: List<CityUiItem> = emptyList(),
        val favoriteCities: List<CityUiItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val noResultsFound: Boolean = false,
        val togglingCityIds: Set<Long> = emptySet(),
    )

    companion object {
        private const val SEARCH_QUERY_KEY = "city_list_search_query"
    }
}