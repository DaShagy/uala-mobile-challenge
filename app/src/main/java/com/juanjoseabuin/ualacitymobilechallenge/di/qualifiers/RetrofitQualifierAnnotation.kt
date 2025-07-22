package com.juanjoseabuin.ualacitymobilechallenge.di.qualifiers

import javax.inject.Qualifier

/**
 * Qualifier for the Retrofit instance specifically configured for Google Static Maps API.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleStaticMapsRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiNinjasRetrofit