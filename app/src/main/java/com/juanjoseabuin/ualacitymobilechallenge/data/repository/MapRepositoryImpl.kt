package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import android.util.Log
import com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.GoogleStaticMapsService
import com.juanjoseabuin.ualacitymobilechallenge.di.qualifiers.GoogleStaticMapsApiKey
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.StaticMapConfig
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.MapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val googleStaticMapsService: GoogleStaticMapsService,
    @GoogleStaticMapsApiKey private val googleStaticMapsApiKey: String // Inject the API key
) : MapRepository {

    companion object {
        private const val TAG = "MapRepositoryImpl"
    }

    override suspend fun getStaticMap(config: StaticMapConfig): ByteArray? {
        Log.i(TAG, "Requesting static map with config: $config")

        val center = "${config.coordinates.lat},${config.coordinates.lon}"
        val size = "${config.width}x${config.height}"
        // Default marker if none provided in config. This ensures a marker is always sent.
        val markerLocation = "${config.coordinates.lat},${config.coordinates.lon}"

        return try {
            val responseBody = googleStaticMapsService.getStaticMap(
                center = center,
                zoom = config.zoom,
                size = size,
                maptype = config.mapType,
                key = googleStaticMapsApiKey,
                markers = config.markers ?: markerLocation // Use config's markers or default
            )
            val mapBytes = responseBody.bytes()
            Log.i(TAG, "Successfully fetched static map (${mapBytes.size} bytes).")
            mapBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching static map for coordinates $center: ${e.message}", e)
            null
        }
    }
}