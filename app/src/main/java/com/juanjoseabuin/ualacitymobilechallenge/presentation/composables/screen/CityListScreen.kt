package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.SearchBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.TopBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel

@Composable
fun CityListScreen(
    viewModel: CityListViewModel,
    onCityCardClick: (Long) -> Unit,
    onCityDetailsButtonClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = {
                    val title =
                        if (uiState.isFilteringByFavorites) "Favorite Cities" else "All Cities"
                    Text(text = title)
                },
                actions = {
                    IconButton(onClick = { viewModel.onToggleFilteringByFavorites() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle filtering by favorites",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
            )
        },
        containerColor = DarkBlue,
        contentColor = DesertWhite,
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr) + 16.dp,
                end = innerPadding.calculateRightPadding(LayoutDirection.Ltr) + 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
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
                    CircularProgressIndicator(
                        color = DesertWhite
                    )
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
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = listState
                ) {
                    val displayedCities =
                        if (uiState.isFilteringByFavorites) uiState.favoriteCities
                        else uiState.displayedCities

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
                            onDetailsButtonClick = {
                                onCityDetailsButtonClick(it.id)
                            }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.isFilteringByFavorites, uiState.searchQuery) {
        listState.scrollToItem(0)
    }
}

@Composable
private fun CityCard(
    city: CityUiItem,
    onCardClick: (Long) -> Unit,
    onFavoriteIconClick: (Long) -> Unit,
    onDetailsButtonClick: (CityUiItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = { onCardClick(city.id) },
        colors = CardColors(
            containerColor = DesertWhite,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Box (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = city.fullName ?: "${city.name}, ${city.country}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Lat: ${city.coord.lat}, Lon: ${city.coord.lon}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkBlue.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDetailsButtonClick(city) }
                    ) {
                        Text("Details")
                    }
                }
            }

            AnimatedFavoriteIcon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp),
                onClick = { onFavoriteIconClick(city.id) },
                isFavorite = city.isFavorite
            )
        }
    }
}

private enum class FavoriteIconState { Filled, Outlined }

@Composable
fun BoxScope.AnimatedFavoriteIcon(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- Animation Logic for IconButton Icon ---
    // Determine the current state for the animation transition
    val favoriteIconState = if (isFavorite) FavoriteIconState.Filled else FavoriteIconState.Outlined

    // Create a transition based on the favoriteIconState
    val transition = updateTransition(favoriteIconState, label = "favoriteIconTransition")

    // Animate the scale of the icon
    val iconScale by transition.animateFloat(
        transitionSpec = {
            // Define the animation spec (e.g., spring for a bouncy effect)
            when {
                FavoriteIconState.Outlined isTransitioningTo FavoriteIconState.Filled ->
                    spring (dampingRatio = Spring .DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                else -> // For other transitions, e.g., filled to outlined
                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            }
        },
        label = "iconScale"
    ) { state ->
        when (state) {
            FavoriteIconState.Filled -> 1.2f // Icon scales up when it becomes filled
            FavoriteIconState.Outlined -> 1.0f // Icon returns to normal size when outlined
        }
    }
    // --- End Animation Logic ---

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 8.dp)
    ) {
        Icon(
            modifier = Modifier
                .requiredSize(24.dp)
                .graphicsLayer(scaleX = iconScale, scaleY = iconScale), // Apply the animated scale here
            imageVector = ImageVector.vectorResource(if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined),
            contentDescription = "Favorite"
        )
    }
}