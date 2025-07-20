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
        width: Int,
        height: Int,
        zoom: Int = 12,
        mapType: String = "roadmap"
    ): ByteArray?
}