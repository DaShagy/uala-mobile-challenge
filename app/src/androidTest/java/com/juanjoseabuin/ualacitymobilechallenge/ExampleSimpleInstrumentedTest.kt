@file:OptIn(ExperimentalCoroutinesApi::class)

package com.juanjoseabuin.ualacitymobilechallenge

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juanjoseabuin.ualacitymobilechallenge.di.AppModule
import com.juanjoseabuin.ualacitymobilechallenge.di.DispatcherModule
import com.juanjoseabuin.ualacitymobilechallenge.di.RepositoryModule
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Coordinates
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.MapRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.MainActivity
import com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.AdaptiveTwoPaneLayout
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityDetailsViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityListViewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.viewmodel.CityMapViewModel
import com.juanjoseabuin.ualacitymobilechallenge.utils.TestDispatcherRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(
    AppModule::class, // Adjust if your AppModule is in another package
    RepositoryModule::class, // Adjust if your RepositoryModule is in another package
    DispatcherModule::class // Your production DispatcherModule
)
class NavigationTests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // Keep AndroidComposeRule for Hilt context

    @get:Rule(order = 2)
    val testDispatcherRule = TestDispatcherRule()

    @Inject
    lateinit var cityRepository: CityRepository

    @Inject
    lateinit var mapRepository: MapRepository

    // Assuming 'setting' is a property you need for AuthenticationScreen, declare it here
    // For testing, you might need a mock/fake instance
    private val setting: Any = "TestSetting" // Replace with actual type and mock if needed

    private val sampleCity1 = City(
        id = 1, name = "City A", country = "Country X", coord = Coordinates(lat = 10.0, lon = 20.0), isFavorite = false
    )
    private val sampleCity2 = City(
        id = 2, name = "City B", country = "Country Y", coord = Coordinates(lat = 30.0, lon = 40.0), isFavorite = true
    )
    private val sampleCities = listOf(sampleCity1, sampleCity2)

    @Before
    fun setup() {
        hiltRule.inject()

        composeTestRule.mainClock.autoAdvance = true

        // Mock repository responses
        coEvery { cityRepository.getPaginatedCities(any(), any(), false, any()) } returns flow {
            delay(1000) // Simulate 1 second of loading time
            emit(sampleCities)
        }
        coEvery { cityRepository.getPaginatedCities(any(), any(), true, any()) } returns flow {
            delay(1000) // Simulate 1 second of loading time
            emit(listOf(sampleCity2))
        }
        coEvery { cityRepository.getCityById(sampleCity1.id) } returns sampleCity1
        coEvery { cityRepository.getCityById(sampleCity2.id) } returns sampleCity2
        coEvery { cityRepository.toggleCityFavoriteStatusById(any()) } returns Unit


        // Explicitly set the content for the test, ensuring it's the only root Composable
        composeTestRule.setContent {
            UalaCityMobileChallengeTheme { // Wrap in your app's theme
                val configuration = LocalConfiguration.current
                val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                val cityListViewModel: CityListViewModel = hiltViewModel()
                val cityDetailsViewModel: CityDetailsViewModel = hiltViewModel()
                val cityMapViewModel: CityMapViewModel = hiltViewModel()

                val navController = rememberNavController()

                // Directly use your AdaptiveTwoPaneLayout, matching MainActivity's structure
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

    @Test
    fun portrait_navigateFromCityListToMapAndBack() {
        composeTestRule.onNodeWithText("Loading initial data...", useUnmergedTree = true)
            .assertIsDisplayed()

        testDispatcherRule.testScope.advanceUntilIdle()

        composeTestRule.onNodeWithText("Loading initial data...", useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("City Explorer").assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleCity1.fullName!!).assertIsDisplayed()

        composeTestRule.onNodeWithText(sampleCity1.fullName!!)
            .performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Map Screen for City ${sampleCity1.id}").assertIsDisplayed() // Using placeholder text

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("City Explorer").assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleCity1.fullName!!).assertIsDisplayed()
    }
}