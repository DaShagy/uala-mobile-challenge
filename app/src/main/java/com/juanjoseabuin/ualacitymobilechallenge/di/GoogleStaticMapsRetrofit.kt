package com.juanjoseabuin.ualacitymobilechallenge.di

import javax.inject.Qualifier

/**
 * Qualifier for the Retrofit instance specifically configured for Google Static Maps API.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleStaticMapsRetrofit