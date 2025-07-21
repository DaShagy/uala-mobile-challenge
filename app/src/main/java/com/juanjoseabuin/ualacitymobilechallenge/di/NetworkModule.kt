package com.juanjoseabuin.ualacitymobilechallenge.di

import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.ApiNinjasService
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.GoogleStaticMapsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Base URL for Google Static Maps API
    private const val BASE_STATIC_MAPS_URL = "https://maps.googleapis.com/maps/api/"
    private const val BASE_API_NINJAS_URL = "https://api.api-ninjas.com/"

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
    fun provideGoogleStaticMapsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_STATIC_MAPS_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @ApiNinjasRetrofit
    fun provideApiNinjasRetrofit(okHttpClient: OkHttpClient): Retrofit {

        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            prettyPrint = true
        }

        return Retrofit.Builder()
            .baseUrl(BASE_API_NINJAS_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleStaticMapsService(@GoogleStaticMapsRetrofit retrofit: Retrofit): GoogleStaticMapsService {
        return retrofit.create(GoogleStaticMapsService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiNinjasService(@ApiNinjasRetrofit retrofit: Retrofit): ApiNinjasService {
        return retrofit.create(ApiNinjasService::class.java)
    }
}