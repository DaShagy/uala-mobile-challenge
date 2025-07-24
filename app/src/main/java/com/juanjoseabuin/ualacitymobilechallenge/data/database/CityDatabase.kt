package com.juanjoseabuin.ualacitymobilechallenge.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CityDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CountryDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.CityEntity
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.CountryEntity

@Database(entities = [CityEntity::class, CountryEntity::class], version = 14, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao
    abstract fun countryDao(): CountryDao

    companion object {
        @Volatile
        private var INSTANCE: CityDatabase? = null

        fun getDatabase(context: Context): CityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CityDatabase::class.java,
                    "city_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}