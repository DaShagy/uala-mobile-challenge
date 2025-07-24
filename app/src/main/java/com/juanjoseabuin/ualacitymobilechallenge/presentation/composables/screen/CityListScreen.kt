package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.CityCard
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.SearchBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils.TopBar
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.SandYellow
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListAction
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListState
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel

@Composable
fun CityListScreenRoot(
    viewModel: CityListViewModel,
    onCityCardClick: (Long) -> Unit,
    onCityDetailsButtonClick: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CityListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is CityListAction.OnCityClick -> onCityCardClick(action.city.id)
                is CityListAction.OnCityDetailsClick -> onCityDetailsButtonClick(action.city.id)
                else -> Unit
            }

            viewModel.onAction(action)
        }
    )
}

@Composable
fun CityListScreen(
    state: CityListState,
    onAction: (CityListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = state.selectedTabIndex) { 2 }
    val allCitiesListState = rememberLazyListState()
    val favoriteCitiesListState = rememberLazyListState()


    LaunchedEffect(state.searchQuery, state.selectedTabIndex) {
        val currentListState = when (state.selectedTabIndex) {
            CityListViewModel.ALL_CITIES_TAB_INDEX -> allCitiesListState
            CityListViewModel.FAVORITE_CITIES_TAB_INDEX -> favoriteCitiesListState
            else -> null
        }
        if (currentListState != null && (state.allCitiesCurrentPage == 0 || state.favoriteCitiesCurrentPage == 0)) {
            currentListState.scrollToItem(0)
        }
    }


    LaunchedEffect(state.selectedTabIndex) {
        pagerState.animateScrollToPage(state.selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.selectedTabIndex) {
            onAction(CityListAction.OnTabSelected(pagerState.currentPage))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = {
                    Text(text = "City Explorer")
                },
            )
        },
        containerColor = DarkBlue,
        contentColor = DesertWhite,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr) + 16.dp,
                    end = innerPadding.calculateRightPadding(LayoutDirection.Ltr) + 16.dp,
                    top = innerPadding.calculateTopPadding(),
                )
                .fillMaxSize(), // Ensure column fills available space
            horizontalAlignment = Alignment.CenterHorizontally // Center search bar and tabs
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                value = state.searchQuery,
                onValueChange = { onAction(CityListAction.OnSearchQueryChange(it)) }, // Use onAction
                placeholder = { Text("Search cities...") },
            )

            // TabRow for "All Cities" and "Favorites"
            Surface( // Use Surface to apply rounded corners to the TabRow background
                modifier = Modifier
                    .fillMaxWidth(),
                color = DesertWhite,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp) // Apply shape here if you want it
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TabRow( // Changed from TabRow to PrimaryTabRow for better theming
                        selectedTabIndex = state.selectedTabIndex,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        containerColor = Color.Transparent, // Ensure transparent if Surface sets background
                        indicator = { tabPositions ->
                            TabRowDefaults.PrimaryIndicator( // Use PrimaryIndicator for consistent theming
                                color = SandYellow,
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[state.selectedTabIndex])
                            )
                        }
                    ) {
                        Tab(
                            selected = state.selectedTabIndex == CityListViewModel.ALL_CITIES_TAB_INDEX,
                            onClick = { onAction(CityListAction.OnTabSelected(CityListViewModel.ALL_CITIES_TAB_INDEX)) },
                            modifier = Modifier.weight(1f),
                            selectedContentColor = SandYellow,
                            unselectedContentColor = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "All Cities", // Use stringResource if available (e.g., R.string.all_cities)
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                        Tab(
                            selected = state.selectedTabIndex == CityListViewModel.FAVORITE_CITIES_TAB_INDEX,
                            onClick = { onAction(CityListAction.OnTabSelected(CityListViewModel.FAVORITE_CITIES_TAB_INDEX)) },
                            modifier = Modifier.weight(1f),
                            selectedContentColor = SandYellow,
                            unselectedContentColor = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Favorites", // Use stringResource if available (e.g., R.string.favorites)
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp)) // Space between tabs and pager

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Let pager take remaining vertical space
                    ) { pageIndex ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            when (pageIndex) {
                                CityListViewModel.ALL_CITIES_TAB_INDEX -> {
                                    // Content for "All Cities" tab
                                    if (state.isLoading) {
                                        CircularProgressIndicator(color = DarkBlue)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Loading initial data...", color = DarkBlue)
                                    } else if (state.error != null) {
                                        Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                                    } else if (state.allCities.isEmpty() && state.selectedTabIndex == CityListViewModel.ALL_CITIES_TAB_INDEX) {
                                        Text(text = "No cities found for \"${state.searchQuery}\"", textAlign = TextAlign.Center)
                                    } else {
                                        CityListContent(
                                            cities = state.allCities,
                                            listState = allCitiesListState,
                                            onAction = onAction,
                                            isLoadingNextPage = state.isLoadingAllCitiesNextPage,
                                            hasMoreCities = state.hasMoreAllCities
                                        )
                                    }
                                }
                                CityListViewModel.FAVORITE_CITIES_TAB_INDEX -> {
                                    // Content for "Favorites" tab
                                    if (state.isLoading) { // Show loading if overall app is loading
                                        CircularProgressIndicator(color = DarkBlue)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Loading initial data...", color = DarkBlue)
                                    } else if (state.error != null) {
                                        Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                                    } else if (state.favoriteCities.isEmpty() && state.selectedTabIndex == CityListViewModel.FAVORITE_CITIES_TAB_INDEX) {
                                        Text(
                                            text = when {
                                                state.searchQuery.isNotBlank() -> "No favorite cities found for \"${state.searchQuery}\""
                                                else -> "You don't have any favorite cities yet."
                                            },
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        CityListContent(
                                            cities = state.favoriteCities,
                                            listState = favoriteCitiesListState,
                                            onAction = onAction,
                                            isLoadingNextPage = state.isLoadingFavoriteCitiesNextPage,
                                            hasMoreCities = state.hasMoreFavoriteCities
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CityListContent(
    cities: List<CityUiItem>,
    listState: LazyListState,
    onAction: (CityListAction) -> Unit,
    isLoadingNextPage: Boolean,
    hasMoreCities: Boolean,
    modifier: Modifier = Modifier
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItemsCount = layoutInfo.totalItemsCount

            // Load more if we are near the end (e.g., within 5 items)
            lastVisibleItemIndex != null && (lastVisibleItemIndex >= totalItemsCount - 5) && hasMoreCities && !isLoadingNextPage
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onAction(CityListAction.OnLoadNextPage)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp).testTag("city_list_scrollable"), // Apply horizontal padding here
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = listState
    ) {
        items(
            count = cities.size,
            key = { index -> cities[index].id }
        ) { index ->
            val city = cities[index]
            CityCard (
                city = city,
                onCardClick = { onAction(CityListAction.OnCityClick(city)) },
                onFavoriteIconClick = { onAction(CityListAction.OnToggleCityFavoriteStatus(city.id)) },
                onDetailsButtonClick = { onAction(CityListAction.OnCityDetailsClick(city)) }
            )
        }
    }
}
