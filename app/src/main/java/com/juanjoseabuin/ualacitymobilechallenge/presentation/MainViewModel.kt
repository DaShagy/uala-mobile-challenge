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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
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

    private val _uiState = MutableStateFlow(CitiesUiState())
    val uiState: StateFlow<CitiesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _togglingCityIds = MutableStateFlow<Set<Long>>(emptySet())

    private val displayedCitiesFlow: Flow<List<CityUiItem>> = _searchQuery
        .debounce(300.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.searchCities(query)
        }
        .combine(_togglingCityIds) { cities, togglingIds ->
            cities.map { city ->
                CityUiItem(
                    city = city,
                    isToggling = togglingIds.contains(city.id)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val favoriteCitiesFlow: Flow<List<CityUiItem>> = repository.getFavoriteCities()
        .combine(_togglingCityIds) { favorites, togglingIds ->
            favorites.map { city ->
                CityUiItem(
                    city = city,
                    isToggling = togglingIds.contains(city.id)
                )
            }
        }
        .stateIn(
            viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val initialLoadingFlow = MutableStateFlow(true)
    private val errorFlow = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try {
                initialLoadingFlow.value = true
                errorFlow.value = null
                repository.ensureDatabasePopulated()
            } catch (e: Exception) {
                errorFlow.value = "Failed to initialize data: ${e.message}"
            } finally {
                initialLoadingFlow.value = false
            }
        }

        combine(
            displayedCitiesFlow,
            favoriteCitiesFlow,
            _searchQuery,
            initialLoadingFlow,
            errorFlow,
            _togglingCityIds
        ) { args: Array<Any?> ->
            val displayedCities = args[0] as List<CityUiItem>
            val favoriteCities = args[1] as List<CityUiItem>
            val searchQuery = args[2] as String
            val isLoading = args[3] as Boolean
            val error = args[4] as String?
            val togglingIds = args[5] as Set<Long>

            CitiesUiState(
                displayedCities = displayedCities,
                favoriteCities = favoriteCities,
                searchQuery = searchQuery,
                isLoading = isLoading,
                error = error,
                togglingCityIds = togglingIds
            )
        }.onEach { newState ->
            if (newState != _uiState.value) {
                _uiState.value = newState
            }
        }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = "An unexpected error occurred: ${e.message}",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
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
        val displayedCities: List<CityUiItem> = emptyList(),
        val favoriteCities: List<CityUiItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val togglingCityIds: Set<Long> = emptySet()
    )
}