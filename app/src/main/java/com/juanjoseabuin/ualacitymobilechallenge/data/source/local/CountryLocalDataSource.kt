package com.juanjoseabuin.ualacitymobilechallenge.data.source.local

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country

interface CountryLocalDataSource {
    suspend fun insertCountry(country: Country)
    suspend fun getCountryByCode(countryCode: String): Country?
    suspend fun updateCountry(country: Country)
    suspend fun getCountryCount(): Int
}