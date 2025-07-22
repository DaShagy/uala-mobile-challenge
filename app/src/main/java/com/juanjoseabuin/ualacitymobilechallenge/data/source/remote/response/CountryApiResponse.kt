package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CountryApiResponse(
    @SerialName("iso2") val iso2: String?,
    @SerialName("surface_area") val surfaceArea: Double?,
    @SerialName("currency") val currency: CurrencyApiResponse?,
    @SerialName("population") val population: Double?,
    @SerialName("name") val name: String?,
    @SerialName("region") val region: String?,
)

@Serializable
data class CurrencyApiResponse(
    @SerialName("code") val code: String?,
    @SerialName("name") val name: String?
)

fun CountryApiResponse.toDomain(): Country {
    return Country(
        countryCode = this.iso2 ?: "",
        name = this.name ?: "Unknown Country",
        population = this.population?.toLong(),
        surfaceArea = this.surfaceArea?.toLong(),
        region = this.region ?: "Unknown Region",
        currency = this.currency?.toDomain() ?: Currency("N/A", "N/A")
    )
}

fun CurrencyApiResponse.toDomain(): Currency {
    return Currency(
        code = this.code ?: "N/A",
        name = this.name ?: "N/A"
    )
}