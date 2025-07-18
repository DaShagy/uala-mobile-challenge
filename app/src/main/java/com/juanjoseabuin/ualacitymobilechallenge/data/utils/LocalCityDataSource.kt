package com.juanjoseabuin.ualacitymobilechallenge.data.utils

import android.content.Context
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class LocalCityDataSource(
    private val context: Context,
    private val json: Json
) : CityDataSource {

    private var _cachedCities: List<City>? = null

    // Mutex to protect access to _cachedCities, ensuring thread-safety during loading.
    private val cacheMutex = Mutex()

    override suspend fun getCities(): Result<List<City>> {

        return cacheMutex.withLock {
            if (_cachedCities == null) {
                val loadResult = withContext(Dispatchers.IO) {
                    try {
                        val inputStream = context.resources.openRawResource(R.raw.cities)
                        val reader = InputStreamReader(inputStream)
                        val jsonString = reader.readText()
                        reader.close()

                        val cities = json.decodeFromString<List<City>>(jsonString)
                        Result.success(cities.sortedBy { it.name }) // Sort once on load
                    } catch (e: Exception) {
                        e.printStackTrace() // Log the error
                        Result.failure(e)
                    }
                }
                if (loadResult.isSuccess) {
                    _cachedCities = loadResult.getOrNull()
                }
                loadResult
            } else {
                Result.success(_cachedCities!!)
            }
        }
    }
}