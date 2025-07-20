package com.juanjoseabuin.ualacitymobilechallenge.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CityListDispatcher

@Module
@InstallIn(SingletonComponent::class) // Installs in SingletonComponent, meaning it lives as long as the application
object DispatcherModule {

    @Provides
    @Singleton
    @CityListDispatcher
    fun provideCityListDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}