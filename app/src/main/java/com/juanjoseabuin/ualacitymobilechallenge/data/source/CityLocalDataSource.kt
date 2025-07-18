package com.juanjoseabuin.ualacitymobilechallenge.data.source

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityLocalDataSource {
    fun getAllCities(): Flow<List<City>>
    suspend fun getCityById(id: Long): City?
    suspend fun updateCity(city: City)
    fun getFavoriteCities(): Flow<List<City>>
    suspend fun insertCities(cities: List<City>)
    suspend fun getCityCount(): Int
}