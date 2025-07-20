package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel

@Composable
fun CityListScreen(
    viewModel: CityListViewModel,
    onCityCardClick: (Long) -> Unit,
    onCityDetailsButtonClick: (Long) -> Unit,
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading initial data...")
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${uiState.error}")
                }
            } else if (uiState.noResultsFound) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No cities found for \"${uiState.searchQuery}\"")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val displayedCities = uiState.displayedCities

                    items(
                        count = displayedCities.size,
                        key = { index -> displayedCities[index].id }
                    ) { index ->
                        val city = displayedCities[index]

                        CityCard(
                            city = city,
                            onCardClick = onCityCardClick,
                            onFavoriteIconClick = {
                                viewModel.toggleCityFavoriteStatus(it)
                            },
                            onDetailsButtonClick = onCityDetailsButtonClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityCard(
    city: CityUiItem,
    onCardClick: (Long) -> Unit,
    onFavoriteIconClick: (Long) -> Unit,
    onDetailsButtonClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = { onCardClick(city.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                    modifier = Modifier.requiredSize(24.dp).clickable { onFavoriteIconClick(city.id) },
                    imageVector = ImageVector.vectorResource(if (city.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined),
                    contentDescription = "Favorite"
                )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${city.name}, ${city.country}",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Lat: ${city.coord.lat}, Lon: ${city.coord.lon}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDetailsButtonClick(city.id) }
                ) {
                    Text("Details")
                }
            }
        }
    }
}