package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.CityDetailsScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.CityListDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.StaticMapDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.screen.CityListScreen
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
    val cityListScreen: @Composable () -> Unit = {
        CityListScreen(
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
                navController.popBackStack()
            }
        )
    }

    val cityDetailScreen: @Composable () -> Unit = {
        CityDetailsScreen(
            onBack = {
                navController.popBackStack()
            },
            onToggleFavoriteStatus = { cityId ->
                cityListViewModel.toggleCityFavoriteStatus(cityId)
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
                    composable<StaticMapDestination> { staticMapScreen() }
                    composable<CityDetailsDestination> { cityDetailScreen() }
                }
            }
        }
    }
}