package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.CityDetailsScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.CityListDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.StaticMapDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.CityListScreenRoot
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.StaticMapScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.CityDetailsDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsAndMapViewModel

@Composable
fun AdaptiveTwoPaneLayout(
    isPortrait: Boolean,
    navController: NavHostController,
    cityListViewModel: CityListViewModel,
    cityDetailsViewModel: CityDetailsAndMapViewModel,
    modifier: Modifier = Modifier
) {
    val cityId = cityDetailsViewModel.uiState.collectAsState().value.city.id
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestinationRoute = currentBackStackEntry?.destination?.route

    val cityListScreen: @Composable () -> Unit = {
        CityListScreenRoot(
            viewModel = cityListViewModel,
            onCityCardClick = { cityId ->
                cityDetailsViewModel.updateCityId(cityId = cityId)
                navController.navigate(StaticMapDestination)
            },
            onCityDetailsButtonClick = { cityId ->
                cityDetailsViewModel.updateCityId(cityId = cityId)
                navController.navigate(CityDetailsDestination)
            }
        )
    }

    val staticMapScreen: @Composable () -> Unit = {
        StaticMapScreen(
            viewModel = cityDetailsViewModel,
            onBack = {
                if (isPortrait) {
                    navController.popBackStack()
                } else {
                    if (navController.currentBackStackEntry?.destination?.route == CityDetailsDestination::class.qualifiedName) {
                        navController.popBackStack()
                    }
                }
            }
        )
    }

    val cityDetailScreen: @Composable () -> Unit = {
        CityDetailsScreen(
            onBack = {
                if (isPortrait) {
                    navController.popBackStack()
                } else {
                    if (navController.currentBackStackEntry?.destination?.route == StaticMapDestination::class.qualifiedName) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(StaticMapDestination) {
                            popUpTo<StaticMapDestination> { inclusive = true } // Clear existing details routes
                        }
                    }
                }
            },
            onToggleFavoriteStatus = { cityId ->
                //cityListViewModel.toggleCityFavoriteStatus(cityId)
            },
            viewModel = cityDetailsViewModel
        )
    }

    if (isPortrait) {
        NavHost(
            navController = navController,
            startDestination = CityListDestination,
            modifier = modifier.fillMaxSize()
        ) {
            composable<CityListDestination> { cityListScreen() }
            composable<StaticMapDestination> { staticMapScreen() }
            composable<CityDetailsDestination> { cityDetailScreen() }
        }
    } else {
        Row(modifier = modifier.fillMaxSize()) {
            Box(Modifier.weight(0.4f)) {
                cityListScreen()
            }
            Box(Modifier.weight(0.6f)) {
                NavHost(
                    navController = navController,
                    startDestination = StaticMapDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable<CityListDestination> {
                        Box(Modifier.fillMaxSize()) {  }
                    }
                    composable<StaticMapDestination> { staticMapScreen() }
                    composable<CityDetailsDestination> { cityDetailScreen() }
                }
            }
        }
    }

    LaunchedEffect(navController, isPortrait, cityId) { // Removed currentDestinationRoute from keys to prevent re-triggering issues inside the effect
        if (!isPortrait) { // Logic specific to landscape mode
            val routeOnEntry = navController.currentBackStackEntry?.destination?.route

            when (routeOnEntry) {
                CityListDestination::class.qualifiedName, null -> {
                    // If coming from CityList or no initial destination, default to map.
                    // This handles initial landscape launch or rotation from portrait list.
                    navController.navigate(StaticMapDestination) {
                        popUpTo<CityListDestination> { inclusive = true } // Clear CityList if it was the source
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                CityDetailsDestination::class.qualifiedName -> {
                    // If we were on CityDetails in portrait, ensure we navigate back to CityDetails
                    // This explicitly brings it to the top of the stack if needed.
                    navController.navigate(CityDetailsDestination) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                StaticMapDestination::class.qualifiedName -> {
                    // If we were on StaticMap in portrait, ensure we navigate back to StaticMap
                    navController.navigate(StaticMapDestination) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }

    LaunchedEffect(isPortrait, navController) { // Added navController as key
        if (isPortrait) { // Logic specific to portrait mode
            val routeOnEntry = navController.currentBackStackEntry?.destination?.route

            when (routeOnEntry) {
                CityListDestination::class.qualifiedName -> {
                    // Already on CityListDestination, do nothing.
                }
                else -> {
                    // If not on CityListDestination (e.g., came from landscape detail),
                    // navigate to CityListDestination and clear the back stack.
                    navController.navigate(CityListDestination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true // Pop the start destination itself
                        }
                        launchSingleTop = true // Avoid creating multiple copies if it's already on top
                        restoreState = true // Restore state if it's being brought back to top
                    }
                }
            }
        }
    }
}