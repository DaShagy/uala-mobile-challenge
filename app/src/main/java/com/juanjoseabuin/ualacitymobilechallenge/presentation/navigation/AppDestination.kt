package com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation

import kotlinx.serialization.Serializable


sealed interface AppDestination

@Serializable
data object CityListDestination : AppDestination

@Serializable
data object StaticMapDestination : AppDestination

@Serializable
data object CityDetailsDestination : AppDestination