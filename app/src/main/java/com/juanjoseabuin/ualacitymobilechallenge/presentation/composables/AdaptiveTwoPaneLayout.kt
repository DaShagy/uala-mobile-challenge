package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.CityListDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.StaticMapDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.CityListScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.StaticMapScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.StaticMapViewModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun AdaptiveTwoPaneLayout(
    isPortrait: Boolean,
    navController: NavHostController,
    cityListViewModel: CityListViewModel,
    staticMapViewModel: StaticMapViewModel,
    modifier: Modifier = Modifier
) {

    val cityListRoute = serializer(CityListDestination::class.java).descriptor.serialName
    val staticMapRoute = serializer(StaticMapDestination::class.java).descriptor.serialName

    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val cityListScreen: @Composable () -> Unit = {
        CityListScreen(
            viewModel = cityListViewModel,
            onCityCardClick = { cityId ->
                staticMapViewModel.updateCityId(cityId = cityId)
                // Internal navigation logic for portrait mode
                if (isPortrait && currentBackStackEntry?.destination?.route == cityListRoute) {
                    navController.navigate(StaticMapDestination)
                }
            },
            onCityDetailsButtonClick = {

            }
        )
    }

    val staticMapScreen: @Composable () -> Unit = {
        StaticMapScreen(
            viewModel = staticMapViewModel,
            onBack = {
                navController.popBackStack()
            }
        )
    }

    if (isPortrait) {
        NavHost(
            navController = navController,
            startDestination = cityListRoute,
            modifier = modifier.fillMaxSize()
        ) {
            composable<CityListDestination> { cityListScreen() }
            composable<StaticMapDestination> { staticMapScreen() }
        }
    } else {
        Row(modifier = modifier.fillMaxSize()) {
            Box(Modifier.weight(0.4f)) { cityListScreen() }
            Box(Modifier.weight(0.6f)) { staticMapScreen() }
        }

        LaunchedEffect(navController, isPortrait, currentBackStackEntry) {
            if (!isPortrait) { // If currently in landscape
                // Access the route from the observed state
                val currentRoute = currentBackStackEntry?.destination?.route

                if (currentRoute == staticMapRoute) {
                    navController.popBackStack(cityListRoute, inclusive = false)
                }
            }
        }
    }
}