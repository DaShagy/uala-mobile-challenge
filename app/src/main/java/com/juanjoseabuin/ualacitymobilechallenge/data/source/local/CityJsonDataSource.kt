package com.juanjoseabuin.ualacitymobilechallenge.data.source.local

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City

interface CityJsonDataSource {
    suspend fun getCities(): Result<List<City>>
}