package com.juanjoseabuin.ualacitymobilechallenge.data.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates

@Entity(
    tableName = "cities",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["name"])
    ]
)
data class CityEntity(
    @PrimaryKey
    val id: Long,
    val country: String,
    val name: String,
    @Embedded
    val coord: CoordEntity,
    val isFavorite: Boolean,
    val isCapital: Boolean? = null,
    val population: Long? = null
)

data class CoordEntity(
    val lon: Double,
    val lat: Double
)

fun CityEntity.toDomain(): City {
    return City(
        id = this.id,
        country = this.country,
        name = this.name,
        coord = Coordinates(lon = this.coord.lon, lat = this.coord.lat),
        isFavorite = this.isFavorite,
        isCapital = isCapital,
        population = population
    )
}

fun City.toEntity(): CityEntity {
    return CityEntity(
        id = this.id,
        country = this.country,
        name = this.name,
        coord = CoordEntity(lon = this.coord.lon, lat = this.coord.lat),
        isFavorite = this.isFavorite
    )
}

fun List<City>.toEntityList(): List<CityEntity> {
    return this.map { it.toEntity() }
}

fun List<CityEntity>.toDomainList(): List<City> {
    return this.map { it.toDomain() }
}