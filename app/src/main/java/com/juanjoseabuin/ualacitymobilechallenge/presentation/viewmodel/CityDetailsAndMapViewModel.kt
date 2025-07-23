package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CountryRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CountryUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CityDetailsAndMapViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val countryRepository: CountryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve initial city ID from savedStateHandle
    private val initialCityId: Long = savedStateHandle.get<Long>(CITY_ID_KEY) ?: -1L

    // Internal mutable StateFlow representing the current UI state
    private val _state = MutableStateFlow(
        CityDetailsState(
            city = CityUiItem(),
            country = CountryUiItem(),
            isLoading = false
        )
    )
    // Expose immutable StateFlow to the UI
    val state: StateFlow<CityDetailsState> = _state.asStateFlow()

    init {
        // Observe changes in the 'city' property of the state to trigger map loading
        state
            .map { it.city }
            .distinctUntilChanged()
            .onEach { city ->
                if (city.id != -1L && _state.value.cityMapImage == null) {
                    // Only load maps if a city is selected AND maps haven't been loaded yet
                    loadStaticMapForCurrentCity()
                } else if (city.id == -1L) {
                    // Reset map images and loading state if city becomes unselected
                    _state.update { it.copy(isLoading = false, error = null, cityMapImage = null, ) }
                }
            }
            .launchIn(viewModelScope) // Launch this flow collector in the ViewModel's scope

        // Load city details if an initial ID exists from SavedStateHandle
        if (initialCityId != -1L) {
            onAction(CityDetailsAction.LoadCityDetails(initialCityId))
        }
    }

    /**
     * Central function to handle all incoming UI actions for City Details.
     */
    fun onAction(action: CityDetailsAction) {
        when (action) {
            is CityDetailsAction.LoadCityDetails -> {
                loadCityDetails(action.cityId)
            }
            is CityDetailsAction.RefreshMapImages -> {
                loadStaticMapForCurrentCity()
            }
            CityDetailsAction.OnBackIconClick -> Unit
        }
    }

    /**
     * Loads city and country details from the repository for a given city ID.
     * @param cityId The ID of the city to load.
     */
    private fun loadCityDetails(cityId: Long) {
        viewModelScope.launch {
            savedStateHandle[CITY_ID_KEY] = cityId // Persist the current city ID

            if (cityId != -1L) {
                _state.update { it.copy(isLoading = true, error = null) }
                try {
                    val cityDomain = cityRepository.getCityById(cityId)
                    if (cityDomain != null) {
                        val countryDomain = countryRepository.getCountry(cityDomain.country)
                        _state.update { it.copy(
                            city = cityDomain.toUiItem(),
                            country = countryDomain?.toUiItem() ?: CountryUiItem(),
                            isLoading = false // Set loading to false once initial data is fetched
                        ) }
                    } else {
                        _state.update { it.copy(city = CityUiItem(), error = "City with ID $cityId not found.", isLoading = false) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching city details for ID $cityId: ${e.message}", e)
                    _state.update { it.copy(city = CityUiItem(), error = "Error fetching city: ${e.message}", isLoading = false) }
                }
            } else {
                // If cityId is -1L, reset the state to default/empty
                _state.update { CityDetailsState(isLoading = false, error = null) }
            }
        }
    }


    /**
     * Loads static map images for the currently selected city.
     * This is called automatically when the city in the state changes.
     */
    private fun loadStaticMapForCurrentCity() {
        viewModelScope.launch {
            val cityUiItem = _state.value.city

            if (cityUiItem.id == -1L) {
                _state.update { it.copy(cityMapImage = null) }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) } // Set loading true for map fetch

            try {
                val cityMapBytes = cityRepository.getStaticMapForCoordinates(
                    coordinates = cityUiItem.coord.toDomain(),
                    width = MAP_IMAGE_WIDTH,
                    height = MAP_IMAGE_HEIGHT,
                    zoom = CITY_MAP_ZOOM,
                    mapType = MAP_TYPE
                )

                _state.update { it.copy(cityMapImage = cityMapBytes) }

                if (cityMapBytes == null) { // Check only one, as both should be null if service fails
                    _state.update { it.copy(error = "Failed to load map image: received empty data.") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading map for city ID ${cityUiItem.id}: ${e.message}", e)
                _state.update { it.copy(error = "Error loading map: ${e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) } // Set loading false after map fetch
            }
        }
    }

    companion object {
        private const val CITY_ID_KEY = "city_details_city_id" // Specific key for CityDetails
        private const val TAG = "CityDetailsViewModel"

        const val MAP_IMAGE_WIDTH = 640
        const val MAP_IMAGE_HEIGHT = 640
        const val CITY_MAP_ZOOM = 12
        const val MAP_TYPE = "roadmap"
    }
}

sealed class CityDetailsAction {
    data class LoadCityDetails(val cityId: Long) : CityDetailsAction()
    data object RefreshMapImages : CityDetailsAction()
    data object OnBackIconClick : CityDetailsAction()
}

data class CityDetailsState(
    val city: CityUiItem = CityUiItem(), // Represents the selected city's UI data
    val country: CountryUiItem = CountryUiItem(), // Represents the city's country UI data
    val cityMapImage: ByteArray? = null, // Byte array for the city's static map image
    val isLoading: Boolean = false, // True if data or maps are currently being loaded
    val error: String? = null, // Any error message to display
    val isTogglingFavorite: Boolean = false // Added for optimistic UI feedback on favorite status
)