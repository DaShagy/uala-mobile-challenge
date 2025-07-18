package com.juanjoseabuin.ualacitymobilechallenge.data.utils

import kotlinx.serialization.json.Json
import java.io.InputStreamReader

object TestUtils {

    // A Json object configured for your needs, e.g., ignoring unknown keys
    val json = Json { ignoreUnknownKeys = true }

    // Use a more generic name if it's for any JSON, or keep it specific if it's only for cities
    // With reified T, you don't need the kClass parameter
    inline fun <reified T> readJsonResource(fileName: String): T {
        // Use a leading slash for resources if they are at the root of the resources directory
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("Resource file not found: $fileName")

        return InputStreamReader(inputStream).use { reader ->
            val jsonString = reader.readText()
            json.decodeFromString<T>(jsonString) // Directly decode using reified T
        }
    }
}