package com.juanjoseabuin.ualacitymobilechallenge.di

import com.juanjoseabuin.ualacitymobilechallenge.data.AppInitializer
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.StaticMapConfig
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CountryRepository
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.MapRepository
import com.juanjoseabuin.ualacitymobilechallenge.utils.TestDispatcherRule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AppModule::class,
        RepositoryModule::class,
        DispatcherModule::class
    ]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideFakeCityRepository(): CityRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideFakeCountryRepository(): CountryRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideFakeMapRepository(): MapRepository {
        return object : MapRepository {
            override suspend fun getStaticMap(config: StaticMapConfig): ByteArray {
                return byteArrayOf() // Returns a valid, empty byte array without MockK proxying
            }
        }
    }

    @Provides
    @Singleton
    fun provideFakeAppInitializer(): AppInitializer {
        val mockInitializer = mockk<AppInitializer>(relaxed = true)
        io.mockk.every { mockInitializer.initializationState } returns MutableStateFlow(AppInitializer.InitializationState.Completed)
        return mockInitializer
    }

    @Provides
    @Singleton
    fun provideTestDispatcher(): TestDispatcher = TestDispatcherRule.currentTestDispatcher

    @Provides
    @Singleton
    @CityListDispatcher
    fun provideCityListTestDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher {
        return testDispatcher
    }
}