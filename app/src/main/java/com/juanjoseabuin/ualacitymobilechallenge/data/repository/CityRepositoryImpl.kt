package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class CityRepositoryImpl(
    private val context: Context
): CityRepository {

    private var cityList: List<City> = listOf()

    override suspend fun getCities(): Result<List<City>> {

        if (cityList.isNotEmpty()) return Result.success(cityList)

        try {
            val inputStream = context.resources.openRawResource(R.raw.cities)
            val reader = InputStreamReader(inputStream)

            val cityListType = object : TypeToken<List<City>>() {}.type
            cityList = Gson().fromJson<List<City>?>(reader, cityListType).sortedBy { it.name }

            withContext(Dispatchers.IO) {
                reader.close()
            }

            return Result.success(cityList)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override suspend fun searchCities(prefix: String): Result<List<City>> {
        val filteredCityList = cityList.filter { it.name.startsWith(prefix, ignoreCase = true) }
        return Result.success(filteredCityList)
    }
}