package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.StaticMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject // Inject the AppInitializer instance
    lateinit var appInitializer: AppInitializer

    private var hasShownLoadingToast = false

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
                    when (state) {
                        AppInitializer.InitializationState.Loading -> {
                            // Only show the "Loading" toast once
                            if (!hasShownLoadingToast) {
                                Toast.makeText(this@MainActivity, "Loading initial data...", Toast.LENGTH_SHORT).show()
                                hasShownLoadingToast = true
                            }
                            keepSplashOn = true // Keep splash screen visible
                        }
                        AppInitializer.InitializationState.Completed -> {
                            Toast.makeText(this@MainActivity, "Data loaded successfully!", Toast.LENGTH_SHORT).show()
                            keepSplashOn = false // Dismiss splash screen
                        }
                        is AppInitializer.InitializationState.Error -> {
                            Toast.makeText(this@MainActivity, "Error loading data: ${state.message}", Toast.LENGTH_LONG).show()
                            keepSplashOn = false // Dismiss splash screen even on error
                        }
                    }
                }
            }
        }

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