package com.juanjoseabuin.ualacitymobilechallenge.presentation.model

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Currency
import kotlinx.serialization.Serializable

@Serializable
data class CountryUiItem(
    val countryCode: String = "",
    val name: String = "",
    val population: Long? = null,
    val surfaceArea: Long? = null,
    val region: String? = null,
    val currency: CurrencyUiItem? = null,
    val squareFlagUrl: String? = null,
    val rectangleFlagUrl: String? = null
)

@Serializable
data class CurrencyUiItem(
    val code: String? = null,
    val name: String? = null
)

fun CurrencyUiItem.toDomain() =
    Currency(
        code = code ?: "",
        name = name ?: ""
    )

fun Currency.toUiItem() =
    CurrencyUiItem(
        code = code,
        name = name
    )

fun CountryUiItem.toDomain() =
    Country(
        countryCode = countryCode,
        name = name,
        population = population,
        region = region,
        surfaceArea = surfaceArea,
        currency = currency?.toDomain() ?: Currency(),
        squareFlagUrl = this.squareFlagUrl,
        rectangleFlagUrl = this.rectangleFlagUrl
    )

fun Country.toUiItem() =
    CountryUiItem(
        countryCode = countryCode,
        name = name,
        population = population,
        surfaceArea = surfaceArea,
        region = region,
        currency = currency.toUiItem(),
        squareFlagUrl = this.squareFlagUrl,
        rectangleFlagUrl = this.rectangleFlagUrl
    )