package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel

private const val TAG = "CityListScreen" // A tag for your Logcat messages

@Composable
fun CityListScreen(
    viewModel: CityListViewModel,
    onCityClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search cities...") }
            )

            if (uiState.isLoading || (uiState.displayedCities.isEmpty() && uiState.searchQuery.isBlank())) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading initial data...")
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${uiState.error}")
                }
            } else if (uiState.noResultsFound) { // Display message when a search yielded no results
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No cities found for \"${uiState.searchQuery}\"")
                }
            } else {
                // Display the list of cities when data is available
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val displayedCities = uiState.displayedCities

                    items(
                        count = displayedCities.size,
                        // It's highly recommended to use a unique ID from your data
                        // for the key, like 'city.id', for better Compose recomposition.
                        // Changed from `it` (index) to `displayedCities[index].id`.
                        key = { index -> displayedCities[index].id }
                    ) { index ->
                        val city = displayedCities[index]
                        val isToggling = uiState.togglingCityIds.contains(city.id)

                        // --- Log when isFavorite status changes for this specific city item ---
                        // This LaunchedEffect will re-run its block whenever city.isFavorite changes
                        // for the city identified by city.id.
                        LaunchedEffect(key1 = city.isFavorite, key2 = city.id) {
                            Log.d(TAG, "City '${city.name}' (ID: ${city.id}) isFavorite status changed to ${city.isFavorite}")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onCityClick(city.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = city.name)
                            Icon(
                                modifier = Modifier
                                    .requiredSize(32.dp)
                                    .clickable(enabled = !isToggling) {
                                        // --- Log when the favorite icon is clicked ---
                                        Log.d(TAG, "Favorite icon clicked for '${city.name}' (ID: ${city.id})")
                                        viewModel.toggleCityFavoriteStatus(city.id)
                                    },
                                imageVector = ImageVector.vectorResource(
                                    if (city.isFavorite) R.drawable.ic_heart_filled
                                    else R.drawable.ic_heart_outlined
                                ),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}