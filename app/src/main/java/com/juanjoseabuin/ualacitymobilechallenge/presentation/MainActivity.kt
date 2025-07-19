package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.data.database.CityDatabase
import com.juanjoseabuin.ualacitymobilechallenge.data.repository.CityRepositoryImpl
import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityLocalDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.source.LocalJsonCityDataSourceImpl
import com.juanjoseabuin.ualacitymobilechallenge.data.source.RoomCityDataSourceImpl
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.utils.ViewModelDelegate.viewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var cityRepository: CityRepository
    private lateinit var cityJsonDataSource: CityJsonDataSource // Renamed from localDataSource
    private lateinit var cityLocalDataSource: CityLocalDataSource// New Room data source
    private lateinit var database: CityDatabase // Room database instance

    private val viewModel by viewModel<MainViewModel> {
        MainViewModel(cityRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = CityDatabase.getDatabase(applicationContext)
        val cityDao = database.cityDao()

        cityJsonDataSource = LocalJsonCityDataSourceImpl(
            applicationContext,
            Json { ignoreUnknownKeys = true }
        )
        cityLocalDataSource = RoomCityDataSourceImpl(cityDao)

        cityRepository = CityRepositoryImpl(
            cityJsonDataSource = cityJsonDataSource,
            cityLocalDataSource = cityLocalDataSource
        )

        enableEdgeToEdge()
        setContent {
            UalaCityMobileChallengeTheme {
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        TextField (
                            modifier = Modifier.fillMaxWidth(),
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) }
                        )

                        if (uiState.isLoading) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (uiState.error != null) {
                            // Display error message if any
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Error: ${uiState.error}")
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    count = uiState.filteredCities.size,
                                    key = { uiState.filteredCities[it].city.id }
                                ) { index ->
                                    val city = uiState.filteredCities[index].city
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = city.name)
                                        Icon(
                                            modifier = Modifier
                                                .requiredSize(32.dp)
                                                .clickable {
                                                    viewModel.toggleFavoriteStatus(city.id)
                                                },
                                            imageVector = ImageVector.vectorResource(
                                                if (city.isFavorite) R.drawable.ic_heart_filled
                                                else R.drawable.ic_heart_outlined
                                            ),
                                            contentDescription = null // Consider providing a meaningful content description for accessibility
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
