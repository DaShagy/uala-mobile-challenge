package com.juanjoseabuin.ualacitymobilechallenge.presentation.model

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityUiItem(
    @SerialName("_id")
    val id: Long = -1L,
    val country: String = "",
    val name: String = "",
    val coord: CoordinatesUiItem = CoordinatesUiItem(),
    val isFavorite: Boolean = false,
    val isCapital: Boolean? = null,
    val population: Long? = null,
    val region: String? = null
)

@Serializable
data class CoordinatesUiItem(
    val lon: Double = Double.NaN,
    val lat: Double = Double.NaN
)

fun CoordinatesUiItem.toDomain() =
    Coordinates(
        lon = lon,
        lat = lat
    )

fun Coordinates.toUiItem() =
    CoordinatesUiItem(
        lon = lon,
        lat = lat
    )

fun CityUiItem.toDomain() =
    City(
        id = id,
        country = country,
        name = name,
        coord = coord.toDomain(),
        isFavorite = isFavorite,
        isCapital = isCapital,
        population = population,
        region = region
    )

fun City.toUiItem() =
    CityUiItem(
        id = id,
        country = country,
        name = name,
        coord = coord.toUiItem(),
        isFavorite = isFavorite,
        isCapital = isCapital,
        population = population,
        region = region
    )