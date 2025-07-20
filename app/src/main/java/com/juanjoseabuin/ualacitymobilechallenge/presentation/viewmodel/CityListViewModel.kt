package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
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

    fun toggleCityFavoriteStatus(cityId: Long) {
        viewModelScope.launch {

            val currentDisplayedStatus = _uiState.value.displayedCities
                .firstOrNull { it.id == cityId }?.isFavorite
                ?: false

            val newOptimisticStatus = !currentDisplayedStatus

            _optimisticFavoriteChanges.update { currentMap ->
                val updatedMap = currentMap + (cityId to newOptimisticStatus)
                updatedMap
            }

            _uiState.update { it.copy(togglingCityIds = it.togglingCityIds + cityId) }

            try {
                repository.toggleCityFavoriteStatusById(cityId)

            } catch (e: Exception) {
                _optimisticFavoriteChanges.update { currentMap ->
                    val updatedMap = currentMap + (cityId to currentDisplayedStatus) // Revert to original status
                    updatedMap
                }
                _uiState.update { it.copy(error = "Failed to toggle favorite status for ID $cityId: ${e.message}") }

            } finally {
                _uiState.update { it.copy(togglingCityIds = it.togglingCityIds - cityId) }
            }
        }
    }

    fun getCityDetails(city: CityUiItem) {
        viewModelScope.launch {
            with(city) {
                try {
                    repository.getCityDetails(
                        id = id,
                        name = name,
                        countryCode = country
                    )

                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            error = "Failed to fetch city details for $name (ID: $id): ${e.message}"
                        )
                    }
                }
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