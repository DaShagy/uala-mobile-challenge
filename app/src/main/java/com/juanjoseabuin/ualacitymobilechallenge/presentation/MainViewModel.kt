package com.juanjoseabuin.ualacitymobilechallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    private val repository: CityRepository
) : ViewModel() {

    // Represents the entire UI state of the screen
    private val _uiState = MutableStateFlow(CitiesUiState())
    val uiState: StateFlow<CitiesUiState> = _uiState.asStateFlow()

    // Holds the current search query entered by the user
    private val _searchQuery = MutableStateFlow("")

    // Tracks cities for which a favorite toggle operation is currently in progress
    private val _togglingCityIds = MutableStateFlow<Set<Long>>(emptySet())

    // State for Static Map
    private val _mapImageBytes = MutableStateFlow<ByteArray?>(null)
    private val _isMapLoading = MutableStateFlow(false)
    private val _mapError = MutableStateFlow<String?>(null)

    // Flow that provides a debounced and distinct list of cities based on the search query
    private val displayedCitiesFlow: Flow<List<CityUiItem>> = _searchQuery
        .debounce(300.milliseconds) // Wait for user to stop typing for 300ms
        .distinctUntilChanged() // Emit only if the query truly changes
        .flatMapLatest { query -> // Switch to a new search flow whenever the query changes
            repository.searchCities(query)
        }
        .map { cities ->
            cities.map { city ->
                CityUiItem(city = city)
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            // Start collecting when there's at least one collector, stop 5 seconds after last collector
            started = SharingStarted.WhileSubscribed(5000)
        )

    // Flow that provides the current list of favorite cities
    private val favoriteCitiesFlow: Flow<List<CityUiItem>> = repository.getFavoriteCities()
        .map { favorites ->
            favorites.map { city -> CityUiItem(city = city) }
        }
        .stateIn(
            viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000)
        )

    // Tracks the initial loading state of the application data
    private val initialLoadingFlow = MutableStateFlow(true)

    // Holds any error message that occurred during data loading or operations
    private val errorFlow = MutableStateFlow<String?>(null)

    init {
        // Launch a coroutine to ensure the database is populated on ViewModel initialization
        viewModelScope.launch {
            try {
                initialLoadingFlow.value = true
                errorFlow.value = null // Clear previous errors
                repository.ensureDatabasePopulated()
            } catch (e: Exception) {
                errorFlow.value = "Failed to initialize data: ${e.message}"
            } finally {
                initialLoadingFlow.value = false
            }
        }

        // Combines multiple data flows into a single UI state flow,
        // which the UI observes for updates.
        combine(
            displayedCitiesFlow,
            favoriteCitiesFlow,
            _searchQuery,
            initialLoadingFlow,
            errorFlow,
            _togglingCityIds, _mapImageBytes,
            _isMapLoading,
            _mapError
        ) { args: Array<Any?> -> // Destructure args array to individual state components
            val displayedCities = args[0] as List<CityUiItem>
            val favoriteCities = args[1] as List<CityUiItem>
            val searchQuery = args[2] as String
            val isLoading = args[3] as Boolean
            val error = args[4] as String?
            val togglingIds = args[5] as Set<Long>
            val mapImage = args[6] as ByteArray?
            val isMapLoading = args[7] as Boolean
            val mapError = args[8] as String?

            // Construct and return the new combined UI state
            CitiesUiState(
                displayedCities = displayedCities,
                favoriteCities = favoriteCities,
                searchQuery = searchQuery,
                isLoading = isLoading,
                error = error,
                togglingCityIds = togglingIds,
                mapImage = mapImage,
                isMapLoading = isMapLoading,
                mapError = mapError
            )
        }.onEach { newState ->
            // Update the main UI state only if it has actually changed
            if (newState != _uiState.value) {
                _uiState.value = newState
            }
        }.catch { e ->
            // Catch any exceptions in the combine/onEach flow and update UI with error
            _uiState.value = _uiState.value.copy(
                error = "An unexpected error occurred: ${e.message}",
                isLoading = false
            )
        }
            .launchIn(viewModelScope) // Launch the collection of the combined flow within ViewModel's scope
    }

    /**
     * Updates the search query. This will trigger a new search operation in the repository.
     * @param query The new search string.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggles the favorite status of a city.
     * Updates the UI optimistically first, then performs the database operation in the background.
     * @param cityId The ID of the city to toggle.
     */
    fun toggleFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            val currentUiState = _uiState.value

            // Optimistically add cityId to the toggling set to show a loading indicator on the UI
            _uiState.value = currentUiState.copy(
                togglingCityIds = currentUiState.togglingCityIds + cityId
            )

            try {
                repository.toggleFavoriteStatus(cityId)
            } catch (e: Exception) {
                // If the operation fails, update the UI state with an error message
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle favorite status for ID $cityId: ${e.message}"
                )
                // e.printStackTrace() // Removed: Redundant with errorFlow update for UI
            } finally {
                // Always remove the cityId from `togglingCityIds` when the operation finishes
                // (whether successful or failed), clearing the loading indicator.
                _uiState.value = _uiState.value.copy(
                    togglingCityIds = _uiState.value.togglingCityIds - cityId
                )
            }
        }
    }

    fun loadMapForCity(cityId: Long) {
        viewModelScope.launch {
            _isMapLoading.value = true
            _mapError.value = null
            _mapImageBytes.value = null // Clear previous map image

            // Find the city's coordinates from the currently displayed cities or repository
            val city = _uiState.value.displayedCities.firstOrNull { it.city.id == cityId }?.city
                ?: repository.getCities().first().firstOrNull { it.id == cityId } // Fallback to fetching all cities
            // Note: The fallback `repository.getCities().first()` will emit the current value from the repository's cache.
            // If the city might not be in the displayedCities or allCities cache, you might need a `getCityById` on repository.

            if (city != null) {
                try {
                    // You can customize width, height, zoom, mapType here if needed
                    val mapBytes = repository.getStaticMapForCoordinates(
                        coordinates = city.coord,
                        width = 400, // Example size
                        height = 200,
                        zoom = 10 // Example zoom
                    )
                    _mapImageBytes.value = mapBytes
                    if (mapBytes == null) {
                        _mapError.value = "Failed to load map image: received empty data."
                    }
                } catch (e: Exception) {
                    _mapError.value = "Error loading map: ${e.message}"
                } finally {
                    _isMapLoading.value = false
                }
            } else {
                _mapError.value = "City with ID $cityId not found to load map."
                _isMapLoading.value = false
            }
        }
    }

    /**
     * Data class representing the complete UI state for the main screen.
     */
    data class CitiesUiState(
        val displayedCities: List<CityUiItem> = emptyList(),
        val favoriteCities: List<CityUiItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val togglingCityIds: Set<Long> = emptySet(),
        val mapImage: ByteArray? = null,
        val isMapLoading: Boolean = false,
        val mapError: String? = null
    )
}