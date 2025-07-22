package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

import android.content.res.Configuration
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.StaticMap
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.TopBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.StaticMapDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAndMapViewModel

@Composable
fun StaticMapScreen(
    viewModel: CityDetailsAndMapViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = { Text(uiState.city.name) },
                navigationIcon = {
                    if (isPortrait) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { _ ->
            StaticMap(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState
            )
    }
}