package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.GoogleStaticMapsService
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CityRepositoryImpl @Inject constructor(
    private val cityJsonDataSource: CityJsonDataSource,
    private val cityLocalDataSource: CityLocalDataSource,
    private val googleStaticMapsService: GoogleStaticMapsService
) : CityRepository {

    private val _allCitiesDbFlow = cityLocalDataSource.getAllCities()
    private val _favoriteCitiesDbFlow = cityLocalDataSource.getFavoriteCities()

    // Holds temporary optimistic updates for favorite status until confirmed by database
    private val _optimisticFavoriteOverrides = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    // Cache for all cities, combining database truth with optimistic overrides
    private val _allCitiesCache = MutableStateFlow<List<City>>(emptyList())
    // Cache for favorite cities, combining database truth with optimistic overrides
    private val _favoriteCitiesCache = MutableStateFlow<List<City>>(emptyList())

    // Coroutine scope for long-running repository operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Combines the raw database flow of all cities with optimistic overrides.
        // It produces a pair: the first element is the raw DB list, the second is the list with overrides applied.
        _allCitiesDbFlow
            .combine(_optimisticFavoriteOverrides) { dbCities, overrides ->
                Pair(dbCities, dbCities.map { city ->
                    val optimisticStatus = overrides[city.id]
                    city.copy(isFavorite = optimisticStatus ?: city.isFavorite)
                })
            }
            .onEach { (dbCities, combinedCities) ->
                _allCitiesCache.value = combinedCities

                // Clean up optimistic overrides once the database has confirmed the status.
                _optimisticFavoriteOverrides.update { currentOverrides ->
                    currentOverrides.filter { (cityId, optimisticStatus) ->
                        // Get the raw database status for this city from the `dbCities` list
                        val rawDbCity = dbCities.firstOrNull { it.id == cityId }
                        val rawDbStatus = rawDbCity?.isFavorite

                        // KEEP the override if:
                        // 1. The city is no longer in the raw DB list (edge case, but handled)
                        // 2. OR the raw DB status does NOT match our optimistic status
                        //    (meaning the DB hasn't propagated the change yet for this city).
                        rawDbStatus == null || rawDbStatus != optimisticStatus
                    }
                }
            }
            .launchIn(repositoryScope)

        // Combines the raw database flow of favorite cities with optimistic overrides.
        // Cleanup of overrides for favorites is handled by the _allCitiesDbFlow logic.
        _favoriteCitiesDbFlow
            .combine(_optimisticFavoriteOverrides) { dbFavorites, overrides ->
                dbFavorites.map { city ->
                    val optimisticStatus = overrides[city.id]
                    city.copy(isFavorite = optimisticStatus ?: city.isFavorite)
                }
            }
            .onEach { combinedFavorites ->
                _favoriteCitiesCache.value = combinedFavorites
            }
            .launchIn(repositoryScope)
    }

    override suspend fun ensureDatabasePopulated() {
        val cityCount = cityLocalDataSource.getCityCount()

        if (cityCount == 0) {
            val citiesResult = cityJsonDataSource.getCities()
            citiesResult.onSuccess { cities ->
                cityLocalDataSource.insertCities(cities)
            }.onFailure { throwable ->
                // Re-throw the exception to be handled by the caller (e.g., ViewModel)
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
                }.sortedBy { it.name }
            }
        }
    }

    override suspend fun toggleFavoriteStatus(cityId: Long) {
        val cityToToggle = _allCitiesCache.value.firstOrNull { it.id == cityId }

        if (cityToToggle != null) {
            val updatedCity = cityToToggle.copy(isFavorite = !cityToToggle.isFavorite)

            // Apply optimistic update immediately to the overrides map
            _optimisticFavoriteOverrides.update { currentOverrides ->
                currentOverrides + (cityId to updatedCity.isFavorite)
            }

            // Launch the database update in a background coroutine
            repositoryScope.launch {
                try {
                    cityLocalDataSource.updateCity(updatedCity)
                } catch (e: Exception) {
                    // If DB update fails, revert the optimistic override
                    _optimisticFavoriteOverrides.update { currentOverrides ->
                        currentOverrides - cityId
                    }
                }
            }
        }
    }

    override fun getFavoriteCities(): Flow<List<City>> {
        return _favoriteCitiesCache
    }


    override suspend fun getStaticMapForCoordinates(
        coordinates: Coordinates,
        width: Int,
        height: Int,
        zoom: Int,
        mapType: String
    ): ByteArray? {
        val center = "${coordinates.lat},${coordinates.lon}"
        val size = "${width}x${height}"

        val apiKey = "AIzaSyDx5dZbOepLOzB-4Kzc73YsIn4w6db1qno"

        // Add a marker for the city location
        val markers = "color:red|label:C|${coordinates.lat},${coordinates.lon}"

        return try {
            val responseBody = googleStaticMapsService.getStaticMap(
                center = center,
                zoom = zoom,
                size = size,
                maptype = mapType,
                markers = markers,
                key = apiKey
            )
            responseBody.bytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}