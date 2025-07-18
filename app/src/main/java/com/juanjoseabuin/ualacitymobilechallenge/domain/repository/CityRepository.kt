package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City

interface CityRepository {
    suspend fun getCities(): Result<List<City>>
    suspend fun searchCities(prefix: String): Result<List<City>>
}