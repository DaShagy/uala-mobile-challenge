package com.juanjoseabuin.ualacitymobilechallenge.data.source.local

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityLocalDataSource {
    suspend fun getCityById(id: Long): City?
    suspend fun updateCity(city: City)
    suspend fun insertCities(cities: List<City>)
    suspend fun getCityCount(): Int
    fun getPaginatedCities(limit: Int, offset: Int, searchQuery: String?): Flow<List<City>>
    fun getPaginatedFavoriteCities(limit: Int, offset: Int): Flow<List<City>>
}