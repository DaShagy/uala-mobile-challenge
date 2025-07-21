package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote

import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response.CityApiResponse
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response.CountryApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiNinjasService {

    @GET("v1/city")
    suspend fun getCityData(
        @Header("X-Api-Key") apiKey: String,
        @Query("name") name: String? = null,
        @Query("country") country: String? = null,
        @Query("min_lat") minLat: Double? = null,
        @Query("max_lat") maxLat: Double? = null,
        @Query("min_lon") minLon: Double? = null,
        @Query("max_lon") maxLon: Double? = null,
    ): List<CityApiResponse>

    @GET("v1/country")
    suspend fun getCountryData(
        @Header("X-Api-Key") apiKey: String,
        @Query("name") name: String
    ): List<CountryApiResponse>
}