package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country

interface CountryRepository {
    suspend fun getCountry(code: String): Country?
}