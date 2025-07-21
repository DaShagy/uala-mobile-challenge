package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
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
    private val repository: CityRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialCityId: Long = savedStateHandle.get<Long>(CITY_ID_KEY) ?: -1L

    private val _uiState = MutableStateFlow(
        CityDetailsUiState(
            city = CityUiItem(), // Default "no city selected" state
            isLoading = false
        )
    )
    val uiState: StateFlow<CityDetailsUiState> = _uiState.asStateFlow()

    init {
        uiState
            .map { it.city }
            .distinctUntilChanged()
            .onEach { city ->
                if (city.id != -1L) {
                    if (uiState.value.mapImage == null) {
                        loadStaticMapForCurrentCity()
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = null, mapImage = null) }
                }
            }
            .launchIn(viewModelScope) // Launch this flow collector in the ViewModel's scope

        if (initialCityId != -1L) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    val cityDomain = repository.getCityById(initialCityId)
                    if (cityDomain != null) {
                        _uiState.update { it.copy(city = cityDomain.toUiItem()) }
                    } else {
                        _uiState.update { it.copy(city = CityUiItem(), error = "Saved city not found.") }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(city = CityUiItem(), error = "Error loading saved city: ${e.message}") }
                }
            }
        }
    }

    fun updateCityId(cityId: Long) {
        viewModelScope.launch {
            savedStateHandle[CITY_ID_KEY] = cityId

            if (cityId != -1L) {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    val cityDomain = repository.getCityById(cityId)
                    if (cityDomain != null) {
                        _uiState.update { it.copy(city = cityDomain.toUiItem()) }
                        loadStaticMapForCurrentCity()
                    } else {
                        _uiState.update { it.copy(city = CityUiItem(), error = "City with ID $cityId not found.") }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(city = CityUiItem(), error = "Error fetching city: ${e.message}") }
                }
            } else {
                _uiState.update { it.copy(city = CityUiItem()) }
            }
        }
    }

    private fun loadStaticMapForCurrentCity() {
        viewModelScope.launch {
            val cityUiItem = uiState.value.city

            if (cityUiItem.id == -1L) {
                _uiState.update { it.copy(isLoading = false, error = null, mapImage = null) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, mapImage = null) }

            try {
                val mapBytes = repository.getStaticMapForCoordinates(
                    coordinates = cityUiItem.coord.toDomain(),
                    width = MAP_IMAGE_WIDTH,
                    height = MAP_IMAGE_HEIGHT,
                    zoom = MAP_ZOOM,
                    mapType = MAP_TYPE
                )
                _uiState.update { it.copy(mapImage = mapBytes) }
                if (mapBytes == null) {
                    _uiState.update { it.copy(error = "Failed to load map image: received empty data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error loading map: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    data class CityDetailsUiState(
        val city: CityUiItem,
        val mapImage: ByteArray? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    companion object {
        private const val CITY_ID_KEY = "static_map_city_id"

        const val MAP_IMAGE_WIDTH = 640
        const val MAP_IMAGE_HEIGHT = 640
        const val MAP_ZOOM = 12
        const val MAP_TYPE = "roadmap"
    }
}