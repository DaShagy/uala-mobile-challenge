package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import kotlinx.coroutines.flow.Flow

interface CityRepository {

    suspend fun isCityDatabaseEmpty(): Boolean

    suspend fun importCitiesIntoDatabase(cities: List<City>)

    fun getCities(): Flow<List<City>>

    fun getFavoriteCities(): Flow<List<City>>

    suspend fun getCityById(id: Long): City?

    suspend fun toggleCityFavoriteStatusById(id: Long)

    suspend fun getStaticMapForCoordinates(
        coordinates: Coordinates,
        width: Int,
        height: Int,
        zoom: Int,
        mapType: String
    ): ByteArray?

    suspend fun getCityDetails(
        id: Long,
        name: String,
        countryCode: String
    ): City?
}