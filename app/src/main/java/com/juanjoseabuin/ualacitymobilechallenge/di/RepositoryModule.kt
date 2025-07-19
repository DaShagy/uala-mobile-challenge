package com.juanjoseabuin.ualacitymobilechallenge.di

import com.juanjoseabuin.ualacitymobilechallenge.data.repository.CityRepositoryImpl
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCityRepository(
        cityRepositoryImpl: CityRepositoryImpl
    ): CityRepository
}