package com.juanjoseabuin.ualacitymobilechallenge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class City(
    @SerialName("_id")
    val id: Long,
    val country: String,
    val name: String,
    val coord: Coordinates,
    val isFavorite: Boolean = false
)

@Serializable
data class Coordinates(
    val lon: Double,
    val lat: Double
)
