package com.juanjoseabuin.ualacitymobilechallenge.domain.model

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("_id")
    val id: Long,
    val country: String,
    val name: String,
    val coord: Coordinates
)

data class Coordinates(
    val lon: Double,
    val lat: Double
)
