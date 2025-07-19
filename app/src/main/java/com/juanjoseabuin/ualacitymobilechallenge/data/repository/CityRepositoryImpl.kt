package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CityRepositoryImpl(
    private val cityJsonDataSource: CityJsonDataSource, // For initial JSON load
    private val cityLocalDataSource: CityLocalDataSource // For Room operations
) : CityRepository {

    // In-memory cache for all cities.
    private val _allCitiesCache = MutableStateFlow<List<City>>(emptyList())

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Collects all cities from the local data source to keep the in-memory cache synchronized with the database.
        repositoryScope.launch {
            cityLocalDataSource.getAllCities().collect { cities ->
                _allCitiesCache.value = cities
            }
        }
    }

    override suspend fun ensureDatabasePopulated() {
        if (cityLocalDataSource.getCityCount() == 0) {
            val citiesResult = cityJsonDataSource.getCities()
            citiesResult.onSuccess { cities ->
                cityLocalDataSource.insertCities(cities)
            }.onFailure { throwable ->
                throw throwable
            }
        }
    }

    override fun getCities(): Flow<List<City>> {
        return _allCitiesCache
    }

    override fun searchCities(prefix: String): Flow<List<City>> {
        return _allCitiesCache.map { allCities ->
            if (prefix.isBlank()) {
                allCities
            } else {
                allCities.filter {
                    it.name.startsWith(prefix, ignoreCase = true)
                }
            }
        }
    }

    override suspend fun toggleFavoriteStatus(cityId: Long) {
        val city = cityLocalDataSource.getCityById(cityId)
        if (city != null) {
            val updatedCity = city.copy(isFavorite = !city.isFavorite)
            cityLocalDataSource.updateCity(updatedCity)
        }
    }

    override fun getFavoriteCities(): Flow<List<City>> {
        return cityLocalDataSource.getFavoriteCities()
    }
}