package com.juanjoseabuin.ualacitymobilechallenge.di

import android.content.Context
import com.juanjoseabuin.ualacitymobilechallenge.data.database.CityDao
import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.LocalJsonCityDataSourceImpl
import com.juanjoseabuin.ualacitymobilechallenge.data.source.RoomCityDataSourceImpl
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
    }
}