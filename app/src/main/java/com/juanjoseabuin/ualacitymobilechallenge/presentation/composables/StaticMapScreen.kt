package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.MainViewModel

@Composable
fun StaticMapScreen(
    cityId: Long,
    modifier: Modifier = Modifier
) {
    StaticMapScreenRoot(cityId = cityId, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaticMapScreenRoot(
    cityId: Long,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cityId) {
        viewModel.loadMapForCity(cityId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Map for City ID: $cityId") }, // You might want a more descriptive title
                navigationIcon = {
                    IconButton(onClick = {
                        // For now, re-trigger map load.
                        // In a real app, this would be `onBackClicked()` or similar
                        // to navigate back.
                        viewModel.loadMapForCity(cityId)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                uiState.isMapLoading -> {
                    CircularProgressIndicator()
                    Text("Loading map...")
                }
                uiState.mapError != null -> {
                    Text(text = "Map Error: ${uiState.mapError}")
                }
                uiState.mapImage != null -> {
                    val imageBitmap: ImageBitmap? = uiState.mapImage?.let {
                        try {
                            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                        } catch (e: Exception) {
                            // Log the error if image decoding fails
                            e.printStackTrace()
                            null
                        }
                    }

                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Static Map for City",
                            modifier = Modifier
                                .fillMaxWidth() // Fill width of parent
                                .weight(1f), // Take available height
                            contentScale = ContentScale.Fit // Scale the image to fit
                        )
                    } else {
                        Text("Failed to display map image.")
                    }
                }
                else -> {
                    // Default state if nothing is loading, no error, and no image
                    Text("Select a city to view its map.")
                }
            }
        }
    }
}