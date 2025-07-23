package com.juanjoseabuin.ualacitymobilechallenge.domain.model

sealed interface StaticMapConfig {
    val coordinates: Coordinates
    val width: Int
    val height: Int
    val zoom: Int
    val mapType: String
    val markers: String?


    data class CityMap(
        override val coordinates: Coordinates,
        override val width: Int,
        override val height: Int,
        override val zoom: Int = 12,
        override val mapType: String = "roadmap",
        override val markers: String? = null
    ) : StaticMapConfig
}