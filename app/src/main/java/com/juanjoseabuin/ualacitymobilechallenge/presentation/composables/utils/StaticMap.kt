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
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.StaticMapDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAndMapViewModel

@Composable
fun StaticMap(
    uiState: CityDetailsAndMapViewModel.CityDetailsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkBlue)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            uiState.city.id == -1L -> {
                Text(
                    text = "Select a city to view its map.",
                    color = DesertWhite
                )
            }

            uiState.isLoading -> {
                CircularProgressIndicator(color = DesertWhite)
                Text(
                    text = "Loading map...",
                    color = DesertWhite
                )
            }

            uiState.error != null -> {
                Text(
                    text = "Map Error: ${uiState.error}",
                    color = DesertWhite
                )
            }

            uiState.mapImage != null -> {
                val imageBitmap: ImageBitmap? = uiState.mapImage.let {
                    try {
                        BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
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