package com.juanjoseabuin.ualacitymobilechallenge.domain.model

data class Country(
    val countryCode: String = "",
    val name: String = "",
    val population: Long = -1L,
    val region: String = "",
    val currency: Currency = Currency()
)

data class Currency(
    val code: String = "",
    val name: String = ""
)