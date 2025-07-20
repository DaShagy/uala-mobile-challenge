package com.juanjoseabuin.ualacitymobilechallenge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class City(
    @SerialName("_id")
    val id: Long = -1L,
    val country: String = "",
    val name: String = "",
    val coord: Coordinates = Coordinates(),
    val isFavorite: Boolean = false,
    val isCapital: Boolean? = null,
    val population: Long? = null
)

@Serializable
data class Coordinates(
    val lon: Double = Double.NaN,
    val lat: Double = Double.NaN
)
