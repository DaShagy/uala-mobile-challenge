package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityApiResponse(
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("country") val country: String, // ISO-3166 alpha-2 code
    @SerialName("population") val population: Long?, // Population might be optional
    @SerialName("is_capital") val isCapital: Boolean?, // Whether it's a capital city
    @SerialName("region") val region: String?
)

fun CityApiResponse.toDomain(id: Long, isFavorite: Boolean): City {
    return City(
        id = id,
        name = this.name,
        country = this.country,
        coord = Coordinates(lon = this.longitude, lat = this.latitude),
        isFavorite = isFavorite,
        isCapital = this.isCapital,
        population = this.population,
        region = this.region
    )
}