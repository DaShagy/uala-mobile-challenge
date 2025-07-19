package com.juanjoseabuin.ualacitymobilechallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
            repository.getCities().collect { cities ->
                _uiState.value = _uiState.value.copy(
                    allCities = cities,
                    isLoading = false // Set loading to false once allCities are available
                )
            }
        }

        // --- Collect Favorite Cities ---
        viewModelScope.launch {
            repository.getFavoriteCities().collect { favorites ->
                _uiState.value = _uiState.value.copy(favoriteCities = favorites)
            }
        }

        // --- Immediate Search Query Update for UI ---
        // This collector ensures that uiState.searchQuery always reflects the IMMEDIATE user input
        viewModelScope.launch {
            _searchQuery.collectLatest { query -> // Use collectLatest to always get the very last emitted value
                _uiState.value = _uiState.value.copy(searchQuery = query)
            }
        }

        // --- Debounced Search for Repository Call ---
        // This collector is for actually triggering the search operation on the repository
        viewModelScope.launch {
            _searchQuery
                .debounce(300L) // Wait for user to stop typing
                .distinctUntilChanged() // Only proceed if the debounced query has changed
                .flatMapLatest { query ->
                    repository.searchCities(query) // Call repository with debounced query
                }
                .collect { filteredCities ->
                    _uiState.value = _uiState.value.copy(filteredCities = filteredCities)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            val currentUiState = _uiState.value
            val cityToToggle = currentUiState.allCities.find { it.id == cityId }

            if (cityToToggle != null) {
                val updatedCity = cityToToggle.copy(isFavorite = !cityToToggle.isFavorite)

                // Optimistically update the UI state immediately
                _uiState.value = currentUiState.copy(
                    // Update the city in all relevant lists in the UI state
                    allCities = currentUiState.allCities.map { city ->
                        if (city.id == cityId) updatedCity else city
                    },
                    filteredCities = currentUiState.filteredCities.map { city ->
                        if (city.id == cityId) updatedCity else city
                    },
                    favoriteCities = if (updatedCity.isFavorite) {
                        // Add to favorites and keep sorted
                        (currentUiState.favoriteCities + updatedCity).sortedBy { it.name }
                    } else {
                        // Remove from favorites
                        currentUiState.favoriteCities.filter { it.id != cityId }
                    }
                )

                try {
                    // Perform the actual database operation.
                    repository.toggleFavoriteStatus(cityId)
                } catch (e: Exception) {
                    _uiState.value = currentUiState // Revert to the state before the optimistic update
                    _uiState.value = _uiState.value.copy(error = "Failed to toggle favorite status: ${e.message}")
                }
            }
        }
    }

    data class CitiesUiState(
        val allCities: List<City> = emptyList(),
        val filteredCities: List<City> = emptyList(),
        val favoriteCities: List<City> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val error: String? = null
    )
}