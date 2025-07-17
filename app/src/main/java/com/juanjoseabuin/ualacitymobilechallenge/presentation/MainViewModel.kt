package com.juanjoseabuin.ualacitymobilechallenge.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class MainViewModel: ViewModel() {

    private val _cities: MutableStateFlow<List<City>> = MutableStateFlow(listOf())
    val cities = _cities.asStateFlow()

    fun loadCities(context: Context) {
        // CMC-01: TODO Move this to its corresponding data source
        viewModelScope.launch {
            try {
                val inputStream = context.resources.openRawResource(R.raw.cities)
                val reader = InputStreamReader(inputStream)

                val cityListType = object : TypeToken<List<City>>() {}.type
                val gson = Gson()
                val cityList: List<City> = gson.fromJson(reader, cityListType)

                _cities.value = cityList

                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}