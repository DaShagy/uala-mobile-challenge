package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response

import com.google.gson.annotations.SerializedName
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates

data class CityApiResponse(
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String, // ISO-3166 alpha-2 code
    @SerializedName("population") val population: Long?, // Population might be optional
    @SerializedName("is_capital") val isCapital: Boolean?, // Whether it's a capital city
    @SerializedName("state") val state: String? // State/Region name
)

fun CityApiResponse.toDomain(id: Long, isFavorite: Boolean): City {
    return City(
        id = id,
        name = this.name,
        country = this.country,
        coord = Coordinates(lon = this.longitude, lat = this.latitude), // Note: API often uses lon, lat order
        isFavorite = isFavorite,
        isCapital = this.isCapital,
        population = this.population
    )
}