package com.juanjoseabuin.ualacitymobilechallenge.di

import android.content.Context
import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CityDao
import com.juanjoseabuin.ualacitymobilechallenge.data.database.dao.CountryDao
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CountryLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.LocalJsonCityDataSourceImpl
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.RoomCityDataSourceImpl
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.RoomCountryDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindCityJsonDataSource(
        localJsonCityDataSourceImpl: LocalJsonCityDataSourceImpl
    ): CityJsonDataSource

    @Binds
    @Singleton
    abstract fun bindCityLocalDataSource(
        roomCityDataSourceImpl: RoomCityDataSourceImpl
    ): CityLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCountryLocalDataSource(
        roomCountryDataSourceImpl: RoomCountryDataSourceImpl
    ): CountryLocalDataSource

    @Module
    @InstallIn(SingletonComponent::class)
    object DataSourceProvidesModule {
        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json { ignoreUnknownKeys = true }
        }

        @Provides
        @Singleton
        fun provideLocalJsonCityDataSourceImpl(
            @ApplicationContext context: Context,
            json: Json
        ): LocalJsonCityDataSourceImpl {
            return LocalJsonCityDataSourceImpl(context, json)
        }

        @Provides
        @Singleton
        fun provideRoomCityDataSourceImpl(
            cityDao: CityDao
        ): RoomCityDataSourceImpl {
            return RoomCityDataSourceImpl(cityDao)
        }

        @Provides
        @Singleton
        fun provideRoomCountryDataSourceImpl(
            countryDao: CountryDao
        ): RoomCountryDataSourceImpl {
            return RoomCountryDataSourceImpl(countryDao)
        }
    }
}