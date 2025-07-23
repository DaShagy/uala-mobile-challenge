package com.juanjoseabuin.ualacitymobilechallenge.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCityById(cityId: Long): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)

    @Update
    suspend fun updateCity(city: CityEntity)

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCityCount(): Int

    @Query("""
        SELECT * FROM cities
        WHERE (
            (:searchQuery IS NULL OR :searchQuery = '')
            OR ((name || ', ' || country) COLLATE NOCASE LIKE :searchQuery || '%')
        )
        AND (:onlyFavorites = 0 OR isFavorite = 1)
        ORDER BY name ASC, country ASC
        LIMIT :limit OFFSET :offset
    """)
    fun getPaginatedCities(
        limit: Int,
        offset: Int,
        searchQuery: String?,
        onlyFavorites: Boolean
    ): Flow<List<CityEntity>>

    @Query("UPDATE cities SET isFavorite = NOT isFavorite WHERE id = :cityId")
    suspend fun toggleFavoriteStatus(cityId: Long)
}