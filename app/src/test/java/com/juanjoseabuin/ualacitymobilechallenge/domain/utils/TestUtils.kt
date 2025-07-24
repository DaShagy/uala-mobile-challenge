package com.juanjoseabuin.ualacitymobilechallenge.domain.utils

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

object TestUtils {
    // A Json object configured for your needs, e.g., ignoring unknown keys
    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> readJsonResource(fileName: String): T {
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("Resource file not found: $fileName")

        return InputStreamReader(inputStream).use { reader ->
            val jsonString = reader.readText()
            json.decodeFromString<T>(jsonString)
        }
    }

    val testCities: List<City> by lazy {
        readJsonResource<List<City>>("test_cities.json").sortedBy { it.fullName }
    }
}