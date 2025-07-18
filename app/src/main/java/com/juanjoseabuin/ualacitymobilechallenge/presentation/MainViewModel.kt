package com.juanjoseabuin.ualacitymobilechallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState
        .onStart { loadCities() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainUiState()
        )

    private val _searchPrefixInternal = MutableStateFlow("")

    init {
        // Observe changes to searchValue with debounce
        viewModelScope.launch {
            _searchPrefixInternal
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { prefix ->
                    executeSearch(prefix)
                }
        }
    }

    fun handleAction(action: MainUiAction) {
        when (action) {
            MainUiAction.LoadInitialCities -> loadCities()
            is MainUiAction.SearchCity -> {
                // Update the search value in the UI state immediately for responsiveness
                _uiState.update { it.copy(searchValue = action.prefix, error = null) }
                // Update the internal flow that triggers the debounced search
                _searchPrefixInternal.value = action.prefix
            }
        }
    }

    private fun loadCities() = viewModelScope.launch(Dispatchers.IO) {
        // Set loading state
        _uiState.update { it.copy(isLoading = true, error = null) }

        cityRepository.getCities().onSuccess { loadedCities ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cities = loadedCities,
                    error = null // Clear any previous errors
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cities = emptyList(),
                    error = throwable.message ?: "Unknown error loading cities"
                )
            }
        }
    }

    private fun executeSearch(prefix: String) = viewModelScope.launch(Dispatchers.IO) {
        cityRepository.searchCities(prefix).onSuccess { filteredCities ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cities = filteredCities,
                    error = null
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cities = emptyList(),
                    error = throwable.message ?: "Unknown error searching cities"
                )
            }
        }
    }

    data class MainUiState(
        val isLoading: Boolean = false,
        val cities: List<City> = emptyList(),
        val searchValue: String = "",
        val error: String? = null
    )

    sealed class MainUiAction {
        data object LoadInitialCities : MainUiAction()
        data class SearchCity(val prefix: String) : MainUiAction()
    }
}