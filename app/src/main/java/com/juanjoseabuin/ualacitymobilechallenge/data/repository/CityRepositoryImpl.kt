package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.utils.CityDataSource
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository

class CityRepositoryImpl(
    private val cityDataSource: CityDataSource
) : CityRepository {

    private var _cachedCities: List<City>? = null

    override suspend fun getCities(): Result<List<City>> {
        if (_cachedCities == null) {
            val result = cityDataSource.getCities()
            if (result.isSuccess) {
                _cachedCities = result.getOrNull()
            }
            return result
        } else {
            return Result.success(_cachedCities!!)
        }
    }

    override suspend fun searchCities(prefix: String): Result<List<City>> {
        val loadResult = getCities()

        return if (loadResult.isSuccess) {
            val filteredCityList = _cachedCities!!.filter { it.name.startsWith(prefix, ignoreCase = true) }
            Result.success(filteredCityList)
        } else {
            Result.failure(loadResult.exceptionOrNull() ?: Exception("Failed to load cities for search"))
        }
    }
}