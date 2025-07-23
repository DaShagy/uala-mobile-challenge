package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.juanjoseabuin.ualacitymobilechallenge.data.AppInitializer
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.AdaptiveTwoPaneLayout
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject // Inject the AppInitializer instance
    lateinit var appInitializer: AppInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashOn = true
        splashScreen.setKeepOnScreenCondition { keepSplashOn }

        lifecycleScope.launch {
            appInitializer.initializeData() // Call the suspend function to populate data
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appInitializer.initializationState.collect { state ->
                    keepSplashOn = when (state) {
                        AppInitializer.InitializationState.Loading -> true
                        else -> false
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            UalaCityMobileChallengeTheme {
                Surface(
                    color = DarkBlue,
                    contentColor = DesertWhite
                ) {
                    val configuration = LocalConfiguration.current
                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                    val cityListViewModel: CityListViewModel = hiltViewModel()
                    val cityDetailsViewModel: CityDetailsViewModel = hiltViewModel()
                    val cityMapViewModel: CityMapViewModel = hiltViewModel()

                    val navController = rememberNavController()

                    AdaptiveTwoPaneLayout(
                        modifier = Modifier.fillMaxSize(),
                        isPortrait = isPortrait,
                        navController = navController,
                        cityListViewModel = cityListViewModel,
                        cityDetailsViewModel = cityDetailsViewModel,
                        cityMapViewModel = cityMapViewModel
                    )
                }
            }
        }
    }
}