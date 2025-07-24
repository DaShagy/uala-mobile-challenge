package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.StaticMap
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.SvgFromUrlImage
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.TopBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAction
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsState

@Composable
fun CityDetailsScreenRoot(
    viewModel: CityDetailsViewModel,
    onBack: () -> Unit,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    CityDetailsScreen(
        state = state,
        onAction = { action ->
            when(action) {
                CityDetailsAction.OnBackIconClick -> onBack()
                else -> Unit
            }

            viewModel.onAction(action)
        }
    )

}

@Composable
fun CityDetailsScreen(
    state: CityDetailsState,
    onAction: (CityDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val city = state.city
    val country = state.country

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        topBar = {
            TopBar(
                title = { Text("City Details") },
                navigationIcon = {
                    IconButton(onClick = { onAction(CityDetailsAction.OnBackIconClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            modifier = Modifier.size(24.dp),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isPortrait) {
                        if (city.isFavorite) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_heart_filled),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Favorite",
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {

                CircularProgressIndicator(color = DarkBlue)
                Spacer(Modifier.height(8.dp))
                Text("Loading city details...")
            } else if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    modifier = Modifier.padding(16.dp)
                )
            } else if (city.id == -1L) { // Default CityUiItem has ID -1L
                Text(
                    "No city selected or details found.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = "${city.name},",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = city.country,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        country.rectangleFlagUrl?.let {
                            SvgFromUrlImage(
                                imageUrl = it,
                                modifier = Modifier.align(Alignment.CenterVertically),
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {

                        DetailRow(label = "Latitude:", value = "${city.coord.lat}")
                        DetailRow(label = "Longitude:", value = "${city.coord.lon}")

                        city.population?.let {
                            DetailRow(label = "Population:", value = it.toString())
                        }

                        city.isCapital?.let {
                            DetailRow(label = "Is Capital:", value = if (it) "Yes" else "No")
                        }

                        city.region?.let {
                            DetailRow(label = "Region:", value = it)
                        }

                        DetailRow(label = "Country:", value = country.name)
                        country.region?.let {
                            DetailRow(label = "Country Region:", value = it)
                        }

                        DetailRow(
                            label = "Country Population:",
                            value = country.population.toString()
                        )

                        country.surfaceArea?.let {
                            DetailRow(label = "Country Surface Area:", value = "$it km2")
                        }
                        country.currency?.let {
                            DetailRow(
                                label = "Currency:",
                                value = "${it.name} ${it.code}"
                            )
                        }

                        if (isPortrait) {
                            Spacer(modifier = Modifier.height(16.dp))
                            StaticMap(
                                modifier = Modifier.height(640.dp),
                                state = state
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
    Spacer(modifier = Modifier.height(4.dp))
}