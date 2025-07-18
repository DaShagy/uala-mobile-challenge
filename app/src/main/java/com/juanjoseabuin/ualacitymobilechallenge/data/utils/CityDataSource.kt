package com.juanjoseabuin.ualacitymobilechallenge.data.utils

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City

interface CityDataSource {
    suspend fun getCities(): Result<List<City>>
}