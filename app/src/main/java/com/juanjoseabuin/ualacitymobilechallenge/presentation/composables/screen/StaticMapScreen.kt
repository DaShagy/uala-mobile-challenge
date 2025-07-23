package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.StaticMap
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.TopBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAction
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAndMapViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsState

@Composable
fun StaticMapScreenRoot(
    viewModel: CityDetailsAndMapViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StaticMapScreen(
        state = state,
        onAction = { action ->
            when (action) {
                CityDetailsAction.OnBackIconClick -> onBack()
                else -> Unit
            }

            viewModel.onAction(action)
        }
    )
}

@Composable
fun StaticMapScreen(
    state: CityDetailsState, // Now receives state directly
    onAction: (CityDetailsAction) -> Unit, // Now receives actions callback
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = { Text(state.city.name) },
                navigationIcon = {
                    if (isPortrait) {
                        IconButton(onClick = { onAction(CityDetailsAction.OnBackIconClick) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) {
        StaticMap(
            modifier = Modifier.fillMaxSize().padding(
                top = it.calculateTopPadding()
            ),
            state = state
        )
    }
}