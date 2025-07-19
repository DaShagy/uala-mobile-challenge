package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    fun getCities(): Flow<List<City>>
    fun searchCities(prefix: String): Flow<List<City>>
    suspend fun toggleFavoriteStatus(cityId: Long)
    fun getFavoriteCities(): Flow<List<City>>
    suspend fun ensureDatabasePopulated()

    suspend fun getStaticMapForCoordinates(
        coordinates: Coordinates,
        width: Int = 600, // Default width
        height: Int = 300, // Default height
        zoom: Int = 14, // Default zoom level
        mapType: String = "roadmap" // Default map type
    ): ByteArray?
}