package com.juanjoseabuin.ualacitymobilechallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: CityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitiesUiState())
    val uiState: StateFlow<CitiesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        // --- Data Loading and Population ---
        viewModelScope.launch {
            try {
                _uiState.value =
                    _uiState.value.copy(isLoading = true) // Set loading to true initially
                repository.ensureDatabasePopulated()
                // isLoading will be set to false after initial population,
                // and then data collection will populate the lists
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize data: ${e.message}"
                )
            }
        }

        // --- Collect All Cities ---
        viewModelScope.launch {
            repository.getCities() // This emits Flow<List<City>>
                .combine(_uiState) { cities, uiState -> // Combine with uiState to get togglingCityIds
                    cities.map { city ->
                        CityUiItem(
                            city = city,
                            isToggling = uiState.togglingCityIds.contains(city.id)
                        )
                    }
                }
                .collect { cityUiItems -> // Collect List<CityUiItem>
                    _uiState.value = _uiState.value.copy(
                        allCities = cityUiItems,
                        isLoading = false // Set loading to false once allCities are available
                    )
                }
        }

        // --- Collect Favorite Cities ---
        viewModelScope.launch {
            repository.getFavoriteCities() // This emits Flow<List<City>>
                .combine(_uiState) { favorites, uiState -> // Combine with uiState
                    favorites.map { city ->
                        CityUiItem(
                            city = city,
                            isToggling = uiState.togglingCityIds.contains(city.id)
                        )
                    }
                }
                .collect { favoriteUiItems -> // Collect List<CityUiItem>
                    _uiState.value = _uiState.value.copy(favoriteCities = favoriteUiItems)
                }
        }

        // --- Immediate Search Query Update for UI ---
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                _uiState.value = _uiState.value.copy(searchQuery = query)
            }
        }

        // --- Debounced Search for Repository Call ---
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    repository.searchCities(query) // This emits Flow<List<City>>
                }
                .combine(_uiState) { filteredCities, uiState -> // Combine with uiState
                    filteredCities.map { city ->
                        CityUiItem(
                            city = city,
                            isToggling = uiState.togglingCityIds.contains(city.id)
                        )
                    }
                }
                .collect { filteredUiItems -> // Collect List<CityUiItem>
                    _uiState.value = _uiState.value.copy(filteredCities = filteredUiItems)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            val currentUiState = _uiState.value

            _uiState.value = currentUiState.copy(
                togglingCityIds = currentUiState.togglingCityIds + cityId
            )

            try {
                repository.toggleFavoriteStatus(cityId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle favorite status for ID $cityId: ${e.message}"
                )
                e.printStackTrace() // Log the error for debugging
            } finally {
                // Always remove the cityId from `togglingCityIds` when the operation finishes
                // (whether successful or failed), clearing the loading indicator.
                _uiState.value = _uiState.value.copy(
                    togglingCityIds = _uiState.value.togglingCityIds - cityId
                )
            }
        }
    }

    data class CitiesUiState(
        val allCities: List<CityUiItem> = emptyList(),
        val filteredCities: List<CityUiItem> = emptyList(),
        val favoriteCities: List<CityUiItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val togglingCityIds: Set<Long> = emptySet()
    )
}