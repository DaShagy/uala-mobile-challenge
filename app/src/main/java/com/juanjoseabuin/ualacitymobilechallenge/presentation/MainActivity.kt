package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.juanjoseabuin.ualacitymobilechallenge.data.repository.CityRepositoryImpl
import com.juanjoseabuin.ualacitymobilechallenge.data.utils.CityDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.utils.LocalCityDataSource
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.utils.ViewModelDelegate.viewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var cityRepository: CityRepository
    private lateinit var localDataSource: CityDataSource

    private val viewModel by viewModel<MainViewModel> {
        MainViewModel(cityRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localDataSource = LocalCityDataSource(
            applicationContext,
            Json { ignoreUnknownKeys = true}
        )

        cityRepository = CityRepositoryImpl(localDataSource)

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
                            value = uiState.searchValue,
                            onValueChange = { viewModel.handleAction(MainViewModel.MainUiAction.SearchCity(it)) }
                        )

                        if (uiState.isLoading) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyColumn {
                                items(
                                    count = uiState.cities.size,
                                    key = { uiState.cities[it].id }
                                ) {
                                    Text(text = uiState.cities[it].name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
