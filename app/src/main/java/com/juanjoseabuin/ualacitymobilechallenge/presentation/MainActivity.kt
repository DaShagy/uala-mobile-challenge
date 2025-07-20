package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.AdaptiveTwoPaneLayout
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.StaticMapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            UalaCityMobileChallengeTheme {

                val configuration = LocalConfiguration.current
                val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                val cityListViewModel: CityListViewModel = hiltViewModel()
                val staticMapViewModel: StaticMapViewModel = hiltViewModel()

                val navController = rememberNavController()

                AdaptiveTwoPaneLayout(
                    modifier = Modifier.fillMaxSize(),
                    isPortrait = isPortrait,
                    navController = navController,
                    cityListViewModel = cityListViewModel,
                    staticMapViewModel = staticMapViewModel
                )
            }
        }
    }
}