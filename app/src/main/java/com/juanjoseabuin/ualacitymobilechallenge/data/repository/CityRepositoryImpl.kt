package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.ApiNinjasCityDetailsService
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.GoogleStaticMapsService
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.di.CityListDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepositoryImpl @Inject constructor(
    private val cityLocalDataSource: CityLocalDataSource,
    private val googleStaticMapsService: GoogleStaticMapsService,
    private val apiNinjasCityDetailsService: ApiNinjasCityDetailsService,
    @CityListDispatcher private val dispatcher: CoroutineDispatcher
) : CityRepository {

    override suspend fun isCityDatabaseEmpty(): Boolean {
        return withContext(dispatcher) {
            cityLocalDataSource.getCityCount() == 0
        }
    }

    override suspend fun importCitiesIntoDatabase(cities: List<City>) {
        return withContext(dispatcher) {
            cityLocalDataSource.insertCities(cities)
        }
    }

    override fun getCities(): Flow<List<City>> {
        return cityLocalDataSource.getAllCities()
    }

    override fun getFavoriteCities(): Flow<List<City>> {
        return cityLocalDataSource.getFavoriteCities()
    }

    override suspend fun getCityById(id: Long): City? {
        return cityLocalDataSource.getCityById(id)
    }

    override suspend fun toggleCityFavoriteStatusById(id: Long) {
        val cityEntity = cityLocalDataSource.getCityById(id) // Fetch the current city from DB
        cityEntity?.let {
            val updatedEntity = it.copy(isFavorite = !it.isFavorite) // Toggle status
            cityLocalDataSource.updateCity(updatedEntity) // Perform the actual database write
        } ?: run {
            throw IllegalArgumentException("City with ID $id not found in repository.")
        }
    }

    override suspend fun getStaticMapForCoordinates(
        coordinates: Coordinates,
        width: Int,
        height: Int,
        zoom: Int,
        mapType: String
    ): ByteArray? {
        val center = "${coordinates.lat},${coordinates.lon}"
        val size = "${width}x${height}"

        val apiKey = "AIzaSyDx5dZbOepLOzB-4Kzc73YsIn4w6db1qno"

        return try {
            val responseBody = googleStaticMapsService.getStaticMap(
                center = center,
                zoom = zoom,
                size = size,
                maptype = mapType,
                key = apiKey
            )
            responseBody.bytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override suspend fun getCityDetails(id: Long, name: String, countryCode: String): City? {
        return withContext(dispatcher) {
            try {

                val apiKey = "PlOT9y16XszsT4aCWGmSVg==vuAS6CopNJJaq7Zj"

                val existingLocalCity = cityLocalDataSource.getCityById(id)

                val citiesResponse = apiNinjasCityDetailsService.getCityData(
                    apiKey = apiKey,
                    name = name,
                    country = countryCode
                )

                val apiCityResponse = citiesResponse.firstOrNull()

                if (apiCityResponse != null) {
                    val updatedCity = apiCityResponse.toDomain(id, existingLocalCity?.isFavorite ?: false)
                    cityLocalDataSource.updateCity(updatedCity)

                    return@withContext updatedCity
                } else {
                    return@withContext existingLocalCity
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }
}