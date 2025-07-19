package com.juanjoseabuin.ualacitymobilechallenge.di

import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.GoogleStaticMapsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Base URL for Google Static Maps API
    private const val BASE_STATIC_MAPS_URL = "https://maps.googleapis.com/maps/api/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    @Provides
    @Singleton
    @GoogleStaticMapsRetrofit
    fun provideStaticMapsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_STATIC_MAPS_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleStaticMapsService(@GoogleStaticMapsRetrofit retrofit: Retrofit): GoogleStaticMapsService {
        return retrofit.create(GoogleStaticMapsService::class.java)
    }
}