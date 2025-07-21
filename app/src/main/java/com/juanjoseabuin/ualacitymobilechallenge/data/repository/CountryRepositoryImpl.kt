package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CountryLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CountryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryRepositoryImpl @Inject constructor(
    private val countryLocalDataSource: CountryLocalDataSource,
): CountryRepository {
    override suspend fun getCountry(code: String): Country? {
        return countryLocalDataSource.getCountryByCode(code)
    }
}