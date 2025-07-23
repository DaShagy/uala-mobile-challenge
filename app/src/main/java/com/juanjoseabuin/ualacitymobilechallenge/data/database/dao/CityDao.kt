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
                (:searchQuery IS NULL OR :searchQuery = '')  -- Condition 1: If search query is empty/null
                OR
                (name COLLATE NOCASE LIKE :searchQuery || '%') -- Condition 2: If search query is not empty, match name
              )
          AND (:onlyFavorites = 0 OR isFavorite = 1) -- Condition 3: Apply favorite filter
        ORDER BY name ASC, country ASC
        LIMIT :limit OFFSET :offset
    """)
    fun getPaginatedCities(
        limit: Int,
        offset: Int,
        searchQuery: String?,
        onlyFavorites: Boolean // New parameter
    ): Flow<List<CityEntity>>
}