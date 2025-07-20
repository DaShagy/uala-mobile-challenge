package com.juanjoseabuin.ualacitymobilechallenge.di

import com.juanjoseabuin.ualacitymobilechallenge.data.AppInitializer
import com.juanjoseabuin.ualacitymobilechallenge.data.source.local.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton // Provide as a singleton to maintain its state throughout the app lifecycle
    fun provideAppInitializer(
        repository: CityRepository,
        cityJsonDataSource: CityJsonDataSource
    ): AppInitializer {
        return AppInitializer(repository, cityJsonDataSource)
    }
}