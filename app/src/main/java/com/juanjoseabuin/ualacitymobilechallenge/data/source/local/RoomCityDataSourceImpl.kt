package com.juanjoseabuin.ualacitymobilechallenge.data.source.local

import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CityDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toDomainList
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntity
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntityList
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCityDataSourceImpl(private val cityDao: CityDao) : CityLocalDataSource {

    override suspend fun getCityById(id: Long): City? {
        return cityDao.getCityById(id)?.toDomain()
    }

    override suspend fun updateCity(city: City) {
        cityDao.updateCity(city.toEntity())
    }

    override suspend fun insertCities(cities: List<City>) {
        cityDao.insertCities(cities.toEntityList())
    }

    override suspend fun getCityCount(): Int {
        return cityDao.getCityCount()
    }

    override fun getPaginatedCities(limit: Int, offset: Int, searchQuery: String?): Flow<List<City>> {
        return cityDao.getPaginatedCities(limit, offset, searchQuery).map { it.toDomainList() }
    }

    override fun getPaginatedFavoriteCities(limit: Int, offset: Int): Flow<List<City>> {
        return cityDao.getPaginatedFavoriteCities(limit, offset).map { it.toDomainList() }
    }
}