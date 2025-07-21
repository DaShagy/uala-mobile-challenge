package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

import android.icu.text.NumberFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAndMapViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: CityDetailsAndMapViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val city = uiState.city

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(city.name.ifEmpty { "City Details" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text("Loading city details...")
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.padding(16.dp)
                )
            } else if (city.id == -1L) { // Default CityUiItem has ID -1L
                Text("No city selected or details found.", modifier = Modifier.padding(16.dp))
            } else {
                // City Name and Country
                Text(
                    text = "${city.name}, ${city.country}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Favorite Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(if (city.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined),
                        contentDescription = "Favorite",
                    )
                    Text("Favorite: ${if (city.isFavorite) "Yes" else "No"}")
                }

                HorizontalDivider (modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                // Geographical Details
                DetailRow(label = "Latitude:", value = "${city.coord.lat}")
                DetailRow(label = "Longitude:", value = "${city.coord.lon}")

                // Enriched Details (Population, Is Capital)
                city.population?.let {
                    DetailRow(
                        label = "Population:",
                        value = NumberFormat.getNumberInstance(Locale.getDefault()).format(it)
                    )
                } ?: DetailRow(label = "Population:", value = "N/A")

                city.isCapital?.let {
                    DetailRow(label = "Is Capital:", value = if (it) "Yes" else "No")
                } ?: DetailRow(label = "Is Capital:", value = "N/A")

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