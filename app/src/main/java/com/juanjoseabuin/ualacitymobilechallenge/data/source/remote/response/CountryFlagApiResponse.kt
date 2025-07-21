package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryFlagApiResponse(
    @SerialName("country") val country: String?,
    @SerialName("square_image_url") val squareImageUrl: String?,
    @SerialName("rectangle_image_url") val rectangleImageUrl: String?,
)
