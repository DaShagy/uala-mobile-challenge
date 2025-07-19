package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    fun getCities(): Flow<List<City>>
    fun searchCities(prefix: String): Flow<List<City>>
    suspend fun toggleFavoriteStatus(cityId: Long)
    fun getFavoriteCities(): Flow<List<City>>
    suspend fun ensureDatabasePopulated()
}