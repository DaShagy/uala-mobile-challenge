package com.juanjoseabuin.ualacitymobilechallenge.presentation.model

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Currency
import kotlinx.serialization.Serializable

@Serializable
data class CountryUiItem(
    val countryCode: String = "",
    val name: String = "",
    val population: Long = -1L,
    val region: String = "",
    val currency: CurrencyUiItem = CurrencyUiItem()
)

@Serializable
data class CurrencyUiItem(
    val code: String = "",
    val name: String = ""
)

fun CurrencyUiItem.toDomain() =
    Currency(
        code = code,
        name = name
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
        currency = currency.toDomain()
    )

fun Country.toUiItem() =
    CountryUiItem(
        countryCode = countryCode,
        name = name,
        population = population,
        region = region,
        currency = currency.toUiItem()
    )