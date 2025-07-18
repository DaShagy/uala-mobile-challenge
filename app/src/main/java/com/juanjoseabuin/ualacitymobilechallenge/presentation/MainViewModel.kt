package com.juanjoseabuin.ualacitymobilechallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel(
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _cities: MutableStateFlow<List<City>> = MutableStateFlow(listOf())
    val cities = _cities.asStateFlow()

    private val _searchValue = MutableStateFlow("")
    val searchValue = _searchValue.asStateFlow()

    init {
        // Observe changes to searchValue with debounce
        viewModelScope.launch {
            _searchValue
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { prefix ->
                    searchCities(prefix)
                }
        }
    }

    fun loadCities() = viewModelScope.launch(Dispatchers.IO) {
        cityRepository.getCities().onSuccess {
            _cities.value = it
        }.onFailure {
            _cities.value = listOf()
        }
    }


    fun searchCity(prefix: String) {
        _searchValue.value = prefix
    }

    private fun searchCities(prefix: String) = viewModelScope.launch(Dispatchers.IO) {
        cityRepository.searchCities(prefix).onSuccess {
            _cities.value = it
        }.onFailure {
            _cities.value = listOf()
        }
    }
}