package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.MainViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem

@Composable
fun CityListScreen(
    modifier: Modifier = Modifier,
    onCityClick: (CityUiItem) -> Unit = {}
) {
    CityListScreenRoot(
        modifier = modifier,
        onCityClick = onCityClick
    )
}

@Composable
private fun CityListScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    onCityClick: (CityUiItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) }
            )

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${uiState.error}")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val displayedCities = uiState.displayedCities

                    items(
                        count = displayedCities.size,
                        key = { displayedCities[it].city.id }
                    ) { index ->
                        val cityItem = displayedCities[index]
                        val city = cityItem.city
                        val isToggling = uiState.togglingCityIds.contains(city.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onCityClick(cityItem) }
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = city.name)
                            Icon(
                                modifier = Modifier
                                    .requiredSize(32.dp)
                                    .clickable(enabled = !isToggling) {
                                        viewModel.toggleFavoriteStatus(city.id)
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