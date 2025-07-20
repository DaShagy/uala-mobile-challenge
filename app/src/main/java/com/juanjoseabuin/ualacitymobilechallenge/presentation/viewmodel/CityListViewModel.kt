package com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.toUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CityListViewModel @Inject constructor(
    private val repository: CityRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CityListUiState(
            isLoading = true,
            searchQuery = savedStateHandle.get<String>(SEARCH_QUERY_KEY) ?: "" // Retrieve value
        )
    )
    val uiState: StateFlow<CityListUiState> = _uiState.asStateFlow()

    init {
        // Initial Data Population
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Set overall loading true, clear any previous error
            try {
                repository.ensureDatabasePopulated()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to initialize data: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        _uiState
            .map { it.searchQuery }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                repository.searchCities(query)
                    .map { cities -> cities.map { it.toUiItem() } }
                    .catch { e ->
                        _uiState.update { it.copy(error = "Error fetching cities: ${e.message}") }
                        emit(emptyList())
                    }
            }
            .onEach { cities ->
                _uiState.update { it.copy(displayedCities = cities) }
            }
            .launchIn(viewModelScope)


        repository.getFavoriteCities()
            .map { favorites -> favorites.map { it.toUiItem() } }
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

    fun toggleFavoriteStatus(cityId: Long) {
        viewModelScope.launch {
            // Optimistically add cityId to the toggling set to show a loading indicator on the UI
            _uiState.update { it.copy(togglingCityIds = it.togglingCityIds + cityId) }
            try {
                repository.toggleFavoriteStatus(cityId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to toggle favorite status for ID $cityId: ${e.message}") }
            } finally {
                _uiState.update { it.copy(togglingCityIds = it.togglingCityIds - cityId) }
            }
        }
    }

    data class CityListUiState(
        val displayedCities: List<CityUiItem> = emptyList(),
        val favoriteCities: List<CityUiItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val togglingCityIds: Set<Long> = emptySet()
    )

    companion object {
        private const val SEARCH_QUERY_KEY = "city_list_search_query"
    }
}