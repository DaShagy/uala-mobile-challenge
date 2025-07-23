package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite

@Composable
fun StaticMap(
    state: StaticMapState,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .background(DarkBlue)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            state.city.id == -1L -> {
                Text(
                    text = "Select a city to view its map.",
                    color = DesertWhite
                )
            }

            state.isLoading -> {
                CircularProgressIndicator(color = DesertWhite)
                Text(
                    text = "Loading map...",
                    color = DesertWhite
                )
            }

            state.error != null -> {
                Text(
                    text = "Map Error: ${state.error}",
                    color = DesertWhite
                )
            }


            state.cityMapImage != null -> {
                val imageBitmap: ImageBitmap? = state.cityMapImage.let {
                    try {
                        BitmapFactory.decodeByteArray(it, 0, it!!.size)?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Static Map for City",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = "Failed to display map image.",
                        color = DesertWhite
                    )
                }
            }

            else -> {
                Text(
                    text = "Map not available.",
                    color = DesertWhite
                )
            }
        }
    }
}

interface StaticMapState {
    val city: CityUiItem
    val isLoading: Boolean
    val error: String?
    val cityMapImage: ByteArray?
}