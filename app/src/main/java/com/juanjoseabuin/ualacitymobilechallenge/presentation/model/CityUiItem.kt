package com.juanjoseabuin.ualacitymobilechallenge.presentation.model

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City

data class CityUiItem(
    val city: City,
    val isToggling: Boolean = false
)