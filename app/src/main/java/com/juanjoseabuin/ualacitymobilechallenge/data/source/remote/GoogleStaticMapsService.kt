package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the Google Static Maps API.
 * This API allows you to embed a Google Maps image on your web page or app without JavaScript.
 *
 * Base URL: https://maps.googleapis.com/maps/api/staticmap
 *
 * For more details, refer to: https://developers.google.com/maps/documentation/static-maps/overview
 */
interface GoogleStaticMapsService {

    /**
     * Fetches a static map image.
     *
     * @param center Defines the center of the map, equidistant from all edges of the map.
     * Format: "latitude,longitude" (e.g., "40.714728,-73.998672")
     * @param zoom Defines the zoom level of the map, with a higher zoom value resulting in a more detailed map.
     * (e.g., 14)
     * @param size Defines the rectangular dimensions of the map image.
     * Format: "widthxheight" (e.g., "600x300")
     * @param maptype Defines the type of map to construct.
     * (e.g., "roadmap", "satellite", "hybrid", "terrain")
     * @param markers Defines one or more markers to attach to the image at specified locations.
     * Format: "color:red|label:S|latitude,longitude"
     * @param key Your Google Maps Static API key.
     * @return A Retrofit Call object that will yield a ResponseBody (the image data).
     */
    @GET("staticmap")
    suspend fun getStaticMap(
        @Query("center") center: String,
        @Query("zoom") zoom: Int,
        @Query("size") size: String, // e.g., "600x300"
        @Query("maptype") maptype: String = "roadmap", // Default to roadmap
        @Query("markers") markers: String? = null, // Optional markers
        @Query("key") key: String
    ): ResponseBody // Use ResponseBody to directly get the image bytes
}