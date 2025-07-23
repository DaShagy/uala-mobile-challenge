package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.StaticMapConfig
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.MapRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.StaticMapState
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CityMapViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    // Internal mutable StateFlow representing the current UI state
    private val _state = MutableStateFlow(
        CityMapState(
            city = CityUiItem(), // Default empty city item
            isLoading = true, // Set to true initially until LoadMap action is processed
            error = null,
            cityMapImage = null
        )
    )

    // Expose immutable StateFlow to the UI
    val state: StateFlow<CityMapState> = _state.asStateFlow()

    /**
     * Central function to handle all incoming UI actions for City Details.
     */
    fun onAction(action: CityMapAction) {
        when (action) {
            is CityMapAction.LoadMap -> {
                Log.d(TAG, "Action: LoadMap received for city ID: ${action.cityId}")
                loadCityDetailsAndMap(action.cityId)
            }

            CityMapAction.OnBackIconClick -> {
                Log.d(TAG, "Action: OnBackIconClick received. UI will handle navigation.")
            }
        }
    }

    /**
     * @param cityId The ID of the city to load.
     */
    private fun loadCityDetailsAndMap(cityId: Long) {
        viewModelScope.launch {
            // Set loading true at the start of the combined operation
            _state.update { it.copy(isLoading = true, error = null, cityMapImage = null) }

            try {
                // 1. Load City Details
                val cityEntity = cityRepository.getCityById(cityId)
                if (cityEntity == null) {
                    _state.update {
                        it.copy(
                            city = CityUiItem(), // Reset city
                            isLoading = false,
                            error = "City with ID $cityId not found."
                        )
                    }
                    Log.e(TAG, "City with ID $cityId not found in database. Cannot load map.")
                    return@launch // Exit if city is not found, no map to load
                }

                val cityUiItem = cityEntity.toUiItem()
                _state.update { it.copy(city = cityUiItem, error = null) }
                Log.d(
                    TAG,
                    "Successfully loaded city details for ID: $cityId - ${cityUiItem.fullName}"
                )


                if (cityUiItem.coord.lat == 0.0 && cityUiItem.coord.lon == 0.0) {
                    _state.update { it.copy(error = "Invalid coordinates for map.") }
                    Log.e(TAG, "Invalid coordinates for city ID ${cityUiItem.id}. Cannot load map.")
                    return@launch // Exit if coordinates are invalid
                }

                val cityMapBytes = mapRepository.getStaticMap(
                    StaticMapConfig.CityMap(
                        coordinates = cityUiItem.coord.toDomain(), // Use the *just fetched* city's coordinates
                        width = MAP_IMAGE_WIDTH,
                        height = MAP_IMAGE_HEIGHT,
                        zoom = CITY_MAP_ZOOM,
                        mapType = MAP_TYPE
                    )
                )

                _state.update { it.copy(cityMapImage = cityMapBytes) }

                if (cityMapBytes == null) {
                    _state.update { it.copy(error = "Failed to load map image: received empty data.") }
                    Log.w(TAG, "Received null map image data for city ID ${cityUiItem.id}.")
                } else if (cityMapBytes.isEmpty()) {
                    _state.update { it.copy(error = "Failed to load map image: received empty byte array.") }
                    Log.w(TAG, "Received empty map image byte array for city ID ${cityUiItem.id}.")
                }
                Log.d(TAG, "Successfully loaded map for city ID ${cityUiItem.id}.")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading city details or map for ID $cityId: ${e.message}", e)
                _state.update {
                    it.copy(
                        city = CityUiItem(), // Reset city on error
                        error = "Error loading city/map: ${e.message}",
                        cityMapImage = null // Clear map image on error
                    )
                }
            } finally {
                _state.update { it.copy(isLoading = false) } // Always set loading false at the end
            }
        }
    }

    companion object {
        private const val TAG = "CityDetailsViewModel"

        const val MAP_IMAGE_WIDTH = 640
        const val MAP_IMAGE_HEIGHT = 640
        const val CITY_MAP_ZOOM = 12
        const val MAP_TYPE = "roadmap"
    }
}

sealed class CityMapAction {
    data class LoadMap(val cityId: Long) : CityMapAction()
    data object OnBackIconClick : CityMapAction()
}

data class CityMapState(
    override val city: CityUiItem = CityUiItem(),
    override val cityMapImage: ByteArray? = null,
    override val isLoading: Boolean = false,
    override val error: String? = null
) : StaticMapState