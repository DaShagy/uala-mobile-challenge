package com.juanjoseabuin.ualacitymobilechallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val cityRepository: CityRepository
): ViewModel() {

    private val _cities: MutableStateFlow<List<City>> = MutableStateFlow(listOf())
    val cities = _cities.asStateFlow()

    fun loadCities() {
        // CMC-01: TODO Move this to its corresponding data source
        viewModelScope.launch {
            cityRepository.getCities().onSuccess {
                _cities.value = it
            }.onFailure {
                _cities.value = listOf()
            }
        }
    }
}