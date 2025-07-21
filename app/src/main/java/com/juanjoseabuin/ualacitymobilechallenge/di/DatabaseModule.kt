package com.juanjoseabuin.ualacitymobilechallenge.di

import android.content.Context
import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CityDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.CityDatabase
import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CountryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCityDatabase(@ApplicationContext context: Context): CityDatabase {
        return CityDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCityDao(cityDatabase: CityDatabase): CityDao {
        return cityDatabase.cityDao()
    }

    @Provides
    @Singleton
    fun provideCountryDao(cityDatabase: CityDatabase): CountryDao {
        return cityDatabase.countryDao()
    }
}