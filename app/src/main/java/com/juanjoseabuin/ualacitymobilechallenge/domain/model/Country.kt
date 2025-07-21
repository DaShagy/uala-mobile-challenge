package com.juanjoseabuin.ualacitymobilechallenge.domain.model

data class Country(
    val countryCode: String = "",
    val name: String = "",
    val population: Long? = null,
    val surfaceArea: Long? = null,
    val region: String? = null,
    val currency: Currency = Currency(),
    val squareFlagUrl: String? = null,
    val rectangleFlagUrl: String? = null,
    val isUpdated: Boolean = false
)

data class Currency(
    val code: String? = null,
    val name: String? = null,
)