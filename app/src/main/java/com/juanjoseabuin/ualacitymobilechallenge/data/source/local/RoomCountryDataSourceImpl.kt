package com.juanjoseabuin.ualacitymobilechallenge.data.source.local

import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CountryDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntity
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country

class RoomCountryDataSourceImpl(private val countryDao: CountryDao): CountryLocalDataSource {

    override suspend fun insertCountry(country: Country) {
        countryDao.insertCountry(country.toEntity())
    }

    override suspend fun getCountryByCode(countryCode: String): Country? {
        return countryDao.getCountryByCode(countryCode)?.toDomain()
    }

    override suspend fun updateCountry(country: Country) {
        countryDao.updateCountry(country.toEntity())
    }

    override suspend fun getCountryCount(): Int {
        return countryDao.getCountryCount()
    }
}