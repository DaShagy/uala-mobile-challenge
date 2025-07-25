package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import android.util.Log
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CountryLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.ApiNinjasService
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.GoogleStaticMapsService
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.di.CityListDispatcher
import com.juanjoseabuin.ualacitymobilechallenge.di.qualifiers.ApiNinjasApiKey
import com.juanjoseabuin.ualacitymobilechallenge.di.qualifiers.GoogleStaticMapsApiKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepositoryImpl @Inject constructor(
    private val cityLocalDataSource: CityLocalDataSource,
    private val countryLocalDataSource: CountryLocalDataSource,
    private val apiNinjasService: ApiNinjasService,
    @ApiNinjasApiKey private val apiNinjasApiKey: String,
    @CityListDispatcher private val dispatcher: CoroutineDispatcher
) : CityRepository {

    companion object {
        private const val TAG = "CityRepositoryImpl"
    }

    override suspend fun isCityDatabaseEmpty(): Boolean {
        Log.i(TAG, "Checking if city database is empty.")
        return withContext(dispatcher) {
            val count = cityLocalDataSource.getCityCount()
            Log.i(TAG, "City count in database: $count")
            count == 0
        }
    }

    override suspend fun importCitiesIntoDatabase(cities: List<City>) {
        Log.i(TAG, "Attempting to import ${cities.size} cities into database.")
        return withContext(dispatcher) {
            cityLocalDataSource.insertCities(cities)
            Log.i(TAG, "Successfully imported ${cities.size} cities.")
        }
    }

    override fun getPaginatedCities(
        limit: Int,
        offset: Int,
        onlyFavorites: Boolean, // This comes directly from the ViewModel/UI
        searchQuery: String?
    ): Flow<List<City>> {
        Log.d(TAG, "Fetching paginated cities from local: limit=$limit, offset=$offset, favorites=$onlyFavorites, query='$searchQuery'")
        // Directly call the unified method on the local data source
        return cityLocalDataSource.getPaginatedCities(limit, offset, searchQuery, onlyFavorites)
            .onEach { cities ->
                Log.d(TAG, "Paginated flow emitted ${cities.size} cities for offset $offset (favorites: $onlyFavorites, query: '$searchQuery').")
            }
    }


    override suspend fun toggleCityFavoriteStatusById(id: Long) {
        withContext(dispatcher) {
            cityLocalDataSource.toggleCityFavoriteStatusById(id)
        }
    }

    override suspend fun getCityById(
        id: Long,
    ): City? {
        Log.i(TAG, "Getting city by ID: $id.")
        val city = getAndProcessCity(id)
        processCountry(city?.country)
        Log.i(TAG, "Returning city for ID $id: ${city?.name ?: "null"}.")
        return city
    }

    private suspend fun getAndProcessCity(cityId: Long): City? {
        Log.i(TAG, "Fetching and processing city with ID: $cityId from local DB.")
        var cityDb: City? = withContext(dispatcher) { cityLocalDataSource.getCityById(cityId) }
        Log.i(TAG, "City ID $cityId from DB: ${cityDb?.name ?: "not found"}. Is updated: ${cityDb?.isUpdated}.")

        if (cityDb?.isUpdated == null || !cityDb.isUpdated) {
            Log.i(TAG, "City ID $cityId is not updated, fetching from Api-Ninjas.")
            try {
                // Construct parameters for API call
                val nameParam = cityDb?.name
                val countryParam = cityDb?.country
                val minLatParam = cityDb?.coord?.lat?.minus(0.1)
                val maxLatParam = cityDb?.coord?.lat?.plus(0.1)
                val minLonParam = cityDb?.coord?.lon?.minus(0.1)
                val maxLonParam = cityDb?.coord?.lon?.plus(0.1)

                Log.d(TAG, "Api-Ninjas request for city ID $cityId: name=$nameParam, country=$countryParam, lat_range=[$minLatParam,$maxLatParam], lon_range=[$minLonParam,$maxLonParam]")

                val citiesRemote = apiNinjasService.getCityData(
                    apiKey = apiNinjasApiKey, // <--- Used injected key
                    name = nameParam,
                    country = countryParam,
                    minLat = minLatParam,
                    maxLat = maxLatParam,
                    minLon = minLonParam,
                    maxLon = maxLonParam
                )
                val cityRemote = citiesRemote.firstOrNull()

                cityRemote?.toDomain(cityId, cityDb?.isFavorite ?: false)?.let{
                    Log.i(TAG, "Successfully fetched remote data for city ID $cityId (${it.name}). Updating local DB.")
                    withContext(dispatcher) { cityLocalDataSource.updateCity(it.copy(isUpdated = true)) }
                    cityDb = withContext(dispatcher) { cityLocalDataSource.getCityById(cityId) } // Re-fetch updated city
                    Log.i(TAG, "Local DB updated for city ID $cityId. New isUpdated status: ${cityDb?.isUpdated}.")
                } ?: run {
                    Log.w(TAG, "No remote city data found for ID $cityId after API call.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching remote city details for cityId $cityId: ${e.message}", e)
            }
        } else {
            Log.i(TAG, "City ID $cityId is already updated. Skipping remote fetch.")
        }
        return cityDb
    }

    private suspend fun processCountry(countryCode: String?) {
        Log.i(TAG, "Processing country data for code: $countryCode.")
        if (countryCode.isNullOrEmpty()) {
            Log.w(TAG, "Country code is null or empty, skipping country processing.")
            return
        }

        val countryDb = withContext(dispatcher) { countryLocalDataSource.getCountryByCode(countryCode) }
        Log.i(TAG, "Country code $countryCode from DB: ${countryDb?.name ?: "not found"}. Is updated: ${countryDb?.isUpdated}.")

        if (countryDb == null || !countryDb.isUpdated) {
            Log.i(TAG, "Country code $countryCode is not updated, fetching from Api-Ninjas.")
            try {
                val country = apiNinjasService.getCountryData(apiNinjasApiKey, countryCode).firstOrNull() // <--- Used injected key
                val countryFlagUrl = apiNinjasService.getCountryFlag(apiNinjasApiKey, countryCode) // <--- Used injected key
                country?.let {
                    Log.i(TAG, "Successfully fetched remote data for country $countryCode (${it.name}). Updating local DB.")
                    withContext(dispatcher) {
                        countryLocalDataSource.insertCountry(
                            it.toDomain()
                                .copy(
                                    squareFlagUrl = countryFlagUrl.squareImageUrl,
                                    rectangleFlagUrl = countryFlagUrl.rectangleImageUrl,
                                    isUpdated = true
                                )
                        )
                    }
                    Log.i(TAG, "Local DB updated for country $countryCode.")
                } ?: run {
                    Log.w(TAG, "No remote country data found for code $countryCode after API call.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching remote country data for code $countryCode: ${e.message}", e)
            }
        } else {
            Log.i(TAG, "Country code $countryCode is already updated. Skipping remote fetch.")
        }
    }
}