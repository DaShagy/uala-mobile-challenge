package com.juanjoseabuin.ualacitymobilechallenge.data.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Currency

@Entity(
    tableName = "countries",
    indices = [
        Index(value = ["countryCode"], unique = true),
    ]
)
data class CountryEntity(
    @PrimaryKey
    val countryCode: String,
    val name: String,
    val population: Long? = null,
    val surfaceArea: Long? = null,
    val region: String? = null,
    @Embedded(prefix = "currency_")
    val currency: CurrencyEntity,
    val squareFlagUrl: String? = null,
    val rectangleFlagUrl: String? = null,
    val isUpdated: Boolean = false
)

data class CurrencyEntity(
    val code: String? = null,
    val name: String? = null,
)

fun CountryEntity.toDomain(): Country {
    return Country(
        countryCode = this.countryCode,
        name = this.name,
        population = this.population?.times(1000L), // Population is saved in db 1000 smaller
        surfaceArea = this.surfaceArea,
        region = this.region,
        currency = Currency(
            code = this.currency.code,
            name = this.currency.name
        ),
        squareFlagUrl = this.squareFlagUrl,
        rectangleFlagUrl = this.rectangleFlagUrl,
        isUpdated = isUpdated
    )
}

fun Country.toEntity(): CountryEntity {
    return CountryEntity(
        countryCode = this.countryCode,
        name = this.name,
        population = this.population,
        surfaceArea = this.surfaceArea,
        region = this.region,
        currency = CurrencyEntity(
            code = this.currency.code,
            name = this.currency.name
        ),
        squareFlagUrl = this.squareFlagUrl,
        rectangleFlagUrl = this.rectangleFlagUrl,
        isUpdated = isUpdated
    )
}

fun List<Country>.toEntityList(): List<CountryEntity> {
    return this.map { it.toEntity() }
}

fun List<CountryEntity>.toDomainList(): List<Country> {
    return this.map { it.toDomain() }
}