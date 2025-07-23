package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import kotlinx.coroutines.flow.Flow

interface CityRepository {

    suspend fun isCityDatabaseEmpty(): Boolean

    suspend fun importCitiesIntoDatabase(cities: List<City>)

    fun getPaginatedCities(
        limit: Int,
        offset: Int,
        onlyFavorites: Boolean,
        searchQuery: String?
    ): Flow<List<City>>

    suspend fun getCityById(id: Long): City?

    suspend fun toggleCityFavoriteStatusById(id: Long)

    suspend fun getStaticMapForCoordinates(
        coordinates: Coordinates,
        width: Int,
        height: Int,
        zoom: Int,
        mapType: String
    ): ByteArray?
}