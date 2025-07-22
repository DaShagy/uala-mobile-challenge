package com.juanjoseabuin.ualacitymobilechallenge.domain.utils

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import com.juanjoseabuin.ualacitymobilechallenge.domain.utils.SearchCityUtils.searchByPrefix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchCityUtilsTest {

    private val sortedCities = TestUtils.testCities

    @Test
    fun `searchByPrefix with empty prefix returns all cities in original order`() {
        val result = sortedCities.searchByPrefix(prefix = "")
        assertEquals(sortedCities.size, result.size)
        assertEquals(sortedCities, result)
        result.assertIsSortedAscending()
    }

    @Test
    fun `searchByPrefix finds multiple cities with common prefix`() {
        // Cities starting with "L" in your JSON:
        // "Land Nordrhein-Westfalen ,DE"
        // "Laspi ,UA"
        // "Lhasa ,CN"
        // "Lichtenrade ,DE"
        // "Lisbon ,PT"
        val result = sortedCities.searchByPrefix(prefix = "L")
        assertEquals(5, result.size)
        assertEquals("Land Nordrhein-Westfalen ,DE", result[0].fullName)
        assertEquals("Laspi ,UA", result[1].fullName)
        assertEquals("Lhasa ,CN", result[2].fullName)
        assertEquals("Lichtenrade ,DE", result[3].fullName)
        assertEquals("Lisbon ,PT", result[4].fullName)
        result.assertIsSortedAscending()
    }

    @Test
    fun `searchByPrefix finds cities with partial prefix`() {
        // Cities starting with "Ka" in your JSON:
        // "Kalanac ,BA"
        // "Karangmangle ,ID"
        // "Karow ,DE"
        // "Kathmandu ,NP"
        val result = sortedCities.searchByPrefix(prefix = "Ka")
        assertEquals(4, result.size)
        assertEquals("Kalanac ,BA", result[0].fullName)
        assertEquals("Karangmangle ,ID", result[1].fullName)
        assertEquals("Karow ,DE", result[2].fullName)
        assertEquals("Kathmandu ,NP", result[3].fullName)
        result.assertIsSortedAscending()
    }

    @Test
    fun `searchByPrefix handles no matching prefix`() {
        val result = sortedCities.searchByPrefix(prefix = "NonExistentCity")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchByPrefix is case-insensitive for prefix`() {
        // Test with "moskva" (lowercase) matching "Moskva"
        val result = sortedCities.searchByPrefix(prefix = "moskva")
        assertEquals(1, result.size)
        assertEquals("Moskva ,RU", result.first().fullName)
        result.assertIsSortedAscending()
    }

    @Test
    fun `searchByPrefix handles a prefix that matches a country-named city`() {
        // "Botswana ,ZA" is in your JSON
        val result = sortedCities.searchByPrefix(prefix = "Botswana")
        assertEquals(1, result.size)
        assertEquals("Botswana ,ZA", result.first().fullName)
        result.assertIsSortedAscending()
    }

    // Helper function to assert list is sorted in ASCENDING order by full name
    private fun List<City>.assertIsSortedAscending() {
        if (this.size <= 1) return
        for (i in 0 until this.size - 1) {
            val compareResult = this[i].fullName.compareTo(this[i + 1].fullName)
            assertTrue(
                "List is not sorted ASCENDING at index $i: '${this[i].fullName}' vs '${this[i+1].fullName}'",
                compareResult <= 0
            )
        }
    }
}