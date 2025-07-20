package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaticMapViewModel @Inject constructor(
    private val repository: CityRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(
        StaticMapUiState(
            cityId = savedStateHandle.get<Long>(CITY_ID_KEY) ?: -1L,
            isLoading = true
        )
    )
    val uiState: StateFlow<StaticMapUiState> = _uiState.asStateFlow()

    fun updateCityId(cityId: Long) {
        if (uiState.value.cityId != cityId || uiState.value.mapImage == null) {
            _uiState.update { it.copy(cityId = cityId) }
            savedStateHandle[CITY_ID_KEY] = cityId
            loadMapForCity()
        }
    }

    private fun loadMapForCity() {
        viewModelScope.launch {
            val cityId = uiState.value.cityId

            if (cityId == -1L) {
                _uiState.update { it.copy(isLoading = false, error = null, mapImage = null) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, mapImage = null) }

            try {
                val city = repository.getCities().first().firstOrNull { it.id == cityId }

                if (city != null) {
                    val mapBytes = repository.getStaticMapForCoordinates(
                        coordinates = city.coord,
                        width = MAP_IMAGE_WIDTH,
                        height = MAP_IMAGE_HEIGHT,
                        zoom = MAP_ZOOM,
                        mapType = MAP_TYPE
                    )
                    _uiState.update { it.copy(mapImage = mapBytes) }
                    if (mapBytes == null) {
                        _uiState.update { it.copy(error = "Failed to load map image: received empty data.") }
                    }
                } else {
                    _uiState.update { it.copy(error = "City with ID $cityId not found to load map.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error loading map: ${e.message}") }
            } finally {
                // Always set map loading to false when the operation finishes.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    data class StaticMapUiState(
        val cityId: Long = -1L,
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