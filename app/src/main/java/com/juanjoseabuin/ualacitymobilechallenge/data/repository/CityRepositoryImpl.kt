package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CountryLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.ApiNinjasService
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
    private val countryLocalDataSource: CountryLocalDataSource,
    private val googleStaticMapsService: GoogleStaticMapsService,
    private val apiNinjasService: ApiNinjasService,
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

    override suspend fun getCityById(
        id: Long,
    ): City? {

        val city = getAndProcessCity(id)
        processCountry(city?.country)

        return city
    }

    private suspend fun getAndProcessCity(cityId: Long): City? {
        var city: City? = cityLocalDataSource.getCityById(cityId)

        val apikey = "PlOT9y16XszsT4aCWGmSVg==vuAS6CopNJJaq7Zj"

        if (city?.isUpdated == false) {
            try {
                apiNinjasService.getCityData(
                    apiKey = apikey,
                    name = city.name,
                    country = city.country,
                    minLat = city.coord.lat - 0.1,
                    maxLat = city.coord.lat + 0.1,
                    minLon = city.coord.lon - 0.1,
                    maxLon = city.coord.lon + 0.1
                ).firstOrNull()?.toDomain(cityId, city.isFavorite)?.let{
                    cityLocalDataSource.updateCity(it.copy(isUpdated = true))
                    city = cityLocalDataSource.getCityById(cityId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error fetching remote city details for cityId $cityId: ${e.message}")
            }
        }
        return city
    }

    private suspend fun processCountry(countryCode: String?) {
        if (countryCode.isNullOrEmpty()) return
        val apikey = "PlOT9y16XszsT4aCWGmSVg==vuAS6CopNJJaq7Zj"

        if (countryLocalDataSource.getCountryByCode(countryCode) == null) {
            try {
                val country = apiNinjasService.getCountryData(apikey, countryCode).firstOrNull()
                country?.let {
                    countryLocalDataSource.insertCountry(it.toDomain())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error fetching remote country data for code $countryCode: ${e.message}")
            }
        }
    }
}