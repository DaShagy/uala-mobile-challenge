package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.juanjoseabuin.ualacitymobilechallenge.data.repository.CityRepositoryImpl
import com.juanjoseabuin.ualacitymobilechallenge.domain.repository.CityRepository
import com.juanjoseabuin.ualacitymobilechallenge.presentation.utils.ViewModelDelegate.viewModel
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.UalaCityMobileChallengeTheme

class MainActivity : ComponentActivity() {

    private lateinit var cityRepository: CityRepository

    private val viewModel by viewModel<MainViewModel> {
        MainViewModel(cityRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cityRepository = CityRepositoryImpl(applicationContext)

        enableEdgeToEdge()
        setContent {
            UalaCityMobileChallengeTheme {
                val cities = viewModel.cities.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        items(
                            count = cities.value.size,
                            key = { cities.value[it].id }
                        ) {
                            Text(text = cities.value[it].name)
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                viewModel.loadCities()
            }
        }
    }
}
