package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.CityListScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.StaticMapScreen
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.CityListDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.navigation.StaticMapDestination
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            UalaCityMobileChallengeTheme {
                val configuration = LocalConfiguration.current
                val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                var selectedCityId by remember { mutableLongStateOf(-1L) }

                if (isPortrait) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = serializer(CityListDestination::class.java).descriptor.serialName,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable<CityListDestination> {
                            CityListScreen(
                                modifier = Modifier.fillMaxSize(),
                                onCityClick = { item ->
                                    val id = item.city.id
                                    navController.navigate(StaticMapDestination(item.city.id))
                                    selectedCityId = id
                                }
                            )
                        }

                        composable<StaticMapDestination> { backStackEntry ->
                            val staticMapDestination = backStackEntry.toRoute<StaticMapDestination>()
                            val cityId = staticMapDestination.cityId

                            StaticMapScreen(
                                cityId = cityId,
                                modifier = Modifier.fillMaxSize(),
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        CityListScreen(
                            modifier = Modifier.weight(0.4f),
                            onCityClick = {
                                selectedCityId = it.city.id
                            }
                        )
                        StaticMapScreen(
                            cityId = selectedCityId,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                }
            }
        }
    }
}