package com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation

import kotlinx.serialization.Serializable


sealed interface AppDestination

@Serializable
object CityListDestination : AppDestination

@Serializable
data class StaticMapDestination(val cityId: Long) : AppDestination