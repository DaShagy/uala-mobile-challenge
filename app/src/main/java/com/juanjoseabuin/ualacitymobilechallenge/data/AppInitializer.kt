package com.juanjoseabuin.ualacitymobilechallenge.data

import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val repository: CityRepository,
    private val cityJsonDataSource: CityJsonDataSource
) {
    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.Loading)
    val initializationState: StateFlow<InitializationState> = _initializationState.asStateFlow()

    suspend fun initializeData() {
        // Only run if not already completed or in an error state (to prevent re-running on config changes if not needed)
        if (_initializationState.value != InitializationState.Completed && (_initializationState.value !is InitializationState.Error)) {
            _initializationState.value = InitializationState.Loading // Set to loading at the start of initialization

            try {
                val isDatabaseEmpty = repository.isCityDatabaseEmpty()

                if (isDatabaseEmpty) {
                    val jsonLoadResult = cityJsonDataSource.getCities()

                    if (jsonLoadResult.isSuccess) {
                        val citiesFromJson = jsonLoadResult.getOrThrow()

                        repository.importCitiesIntoDatabase(citiesFromJson)
                        _initializationState.value = InitializationState.Completed
                    } else {
                        val errorMessage = "Failed to load cities from JSON: ${jsonLoadResult.exceptionOrNull()?.message}"
                        _initializationState.value = InitializationState.Error(errorMessage)
                    }
                } else {
                    _initializationState.value = InitializationState.Completed
                }
            } catch (e: Exception) {
                val errorMessage = "Error during app initialization: ${e.message}"
                _initializationState.value = InitializationState.Error(errorMessage)
            }
        }
    }

    sealed class InitializationState {
        data object Loading : InitializationState()
        data object Completed : InitializationState()
        data class Error(val message: String) : InitializationState()
    }
}