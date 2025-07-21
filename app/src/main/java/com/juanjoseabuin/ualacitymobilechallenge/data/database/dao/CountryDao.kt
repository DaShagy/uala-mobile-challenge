package com.juanjoseabuin.ualacitymobilechallenge.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.CountryEntity

@Dao
interface CountryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: CountryEntity)

    @Update
    suspend fun updateCountry(country: CountryEntity)

    @Query("SELECT * FROM countries WHERE countryCode = :countryCode")
    suspend fun getCountryByCode(countryCode: String): CountryEntity?

    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCountryCount(): Int
}