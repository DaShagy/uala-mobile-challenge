package com.juanjoseabuin.ualacitymobilechallenge.data.source.local

import com.juanjoseabuin.ualacitymobilechallenge.data.database.CityDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toDomainList
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntity
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntityList
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCityDataSourceImpl(private val cityDao: CityDao) : CityLocalDataSource {

    override fun getAllCities(): Flow<List<City>> {
        return cityDao.getAllCities().map { it.toDomainList() }
    }

    override suspend fun getCityById(id: Long): City? {
        return cityDao.getCityById(id)?.toDomain()
    }

    override suspend fun updateCity(city: City) {
        cityDao.updateCity(city.toEntity())
    }

    override fun getFavoriteCities(): Flow<List<City>> {
        return cityDao.getFavoriteCities().map { it.toDomainList() }
    }

    override suspend fun insertCities(cities: List<City>) {
        cityDao.insertCities(cities.toEntityList())
    }

    override suspend fun getCityCount(): Int {
        return cityDao.getCityCount()
    }
}