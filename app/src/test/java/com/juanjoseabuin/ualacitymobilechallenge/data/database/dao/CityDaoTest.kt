package com.juanjoseabuin.ualacitymobilechallenge.data.database.dao // Adjust package as needed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.juanjoseabuin.ualacitymobilechallenge.data.database.CityDatabase
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toDomain
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntity
import com.juanjoseabuin.ualacitymobilechallenge.data.database.entities.toEntityList
import com.juanjoseabuin.ualacitymobilechallenge.domain.utils.TestUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CityDaoTest {

    private lateinit var database: CityDatabase
    private lateinit var cityDao: CityDao

    private suspend fun <T> awaitFlowFirst(flow: kotlinx.coroutines.flow.Flow<T>): T {
        return flow.first()
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CityDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cityDao = database.cityDao()

        // Insert all TestUtils.testCities at the beginning of each test
        runBlocking {
            cityDao.insertCities(TestUtils.testCities.toEntityList())
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `getCityCount returns correct total after initial population`() = runBlocking {
        val count = cityDao.getCityCount()
        assertEquals(TestUtils.testCities.size, count)
    }

    @Test
    fun `getCityById returns correct city when it exists from testCities`() = runBlocking {
        // Pick a city from the pre-populated list, e.g., Hurzuf
        val cityToFind = TestUtils.testCities.first { it.id == 707860L }

        val retrievedCity = cityDao.getCityById(cityToFind.id)
        assertNotNull(retrievedCity)
        assertEquals(cityToFind.name, retrievedCity?.name)
        assertEquals(cityToFind.country, retrievedCity?.country)
        assertEquals(cityToFind.coord.lon, retrievedCity?.coord?.lon)
        assertEquals(cityToFind.coord.lat, retrievedCity?.coord?.lat)
        assertEquals(cityToFind.isFavorite, retrievedCity?.isFavorite)
    }

    @Test
    fun `getCityById returns null when city does not exist in testCities`() = runBlocking {
        val retrievedCity = cityDao.getCityById(999999999L) // An ID not in TestUtils.testCities
        assertNull(retrievedCity)
    }

    @Test
    fun `updateCity updates existing city from testCities`() = runBlocking {
        // Pick a city to update (e.g., Novinki, id=519188)
        val initialCity = TestUtils.testCities.first { it.id == 519188L }

        // Create an updated version of that city
        val updatedCity = initialCity.copy(name = "New Novinki", isFavorite = true, population = 500000L)
        cityDao.updateCity(updatedCity.toEntity())

        val retrievedCity = cityDao.getCityById(initialCity.id)
        assertNotNull(retrievedCity)
        assertEquals("New Novinki", retrievedCity?.name)
        assertEquals(true, retrievedCity?.isFavorite)
        assertEquals(500000L, retrievedCity?.population)
    }

    @Test
    fun `toggleFavoriteStatus changes isFavorite status for a city from testCities`() = runBlocking {
        // Pick a city that is initially NOT favorite (e.g., Gorkhā, id=1283378)
        val cityIdToToggle = 1283378L
        var initialCity = TestUtils.testCities.first { it.id == cityIdToToggle }

        // Ensure its initial state is false for this test (update if necessary, though it should be false by default)
        if (initialCity.isFavorite) {
            cityDao.updateCity(initialCity.copy(isFavorite = false).toEntity())
            initialCity = cityDao.getCityById(cityIdToToggle)?.toDomain()!! // Re-fetch to confirm state
        }
        assertEquals(false, initialCity.isFavorite)

        // Toggle from false to true
        cityDao.toggleFavoriteStatus(cityIdToToggle)
        var retrievedCity = cityDao.getCityById(cityIdToToggle)
        assertNotNull(retrievedCity)
        assertEquals(true, retrievedCity?.isFavorite)

        // Toggle from true to false
        cityDao.toggleFavoriteStatus(cityIdToToggle)
        retrievedCity = cityDao.getCityById(cityIdToToggle)
        assertNotNull(retrievedCity)
        assertEquals(false, retrievedCity?.isFavorite)
    }


    @Test
    fun `getPaginatedCities returns correct paginated results with no filter`() = runBlocking {
        // Data is already inserted in @Before via TestUtils.testCities

        val totalCities = TestUtils.testCities.size
        val limit = 5 // Example limit
        var offset = 0

        // Iterate through pages
        while (offset < totalCities) {
            val currentPage = awaitFlowFirst(cityDao.getPaginatedCities(limit = limit, offset = offset, searchQuery = null, onlyFavorites = false))
            val expectedPageSize = minOf(limit, totalCities - offset)
            assertEquals("Page size incorrect for offset $offset", expectedPageSize, currentPage.size)

            for (i in 0 until expectedPageSize) {
                assertEquals( "City name incorrect at index $i for offset $offset", TestUtils.testCities[offset + i].name, currentPage[i].name)
                assertEquals("Country incorrect at index $i for offset $offset", TestUtils.testCities[offset + i].country, currentPage[i].country)
            }
            offset += limit
        }
    }

    @Test
    fun `getPaginatedCities filters by search query case insensitively`() = runBlocking {
        // Data is already inserted in @Before via TestUtils.testCities

        // Example search for cities starting with "a" (case-insensitive)
        val result = awaitFlowFirst(cityDao.getPaginatedCities(
            limit = TestUtils.testCities.size, offset = 0, searchQuery = "a", onlyFavorites = false
        ))

        val expectedCitiesStartingWithA = TestUtils.testCities.filter {
            it.fullName.startsWith("a", ignoreCase = true)
        }.sortedBy { it.fullName } // Ensure sorted as DAO query

        assertEquals(expectedCitiesStartingWithA.size, result.size)
        expectedCitiesStartingWithA.forEachIndexed { index, city ->
            assertEquals(city.name, result[index].name)
            assertEquals(city.country, result[index].country)
        }

        // Example search for "ma" (case-insensitive)
        val resultMa = awaitFlowFirst(cityDao.getPaginatedCities(
            limit = TestUtils.testCities.size, offset = 0, searchQuery = "ma", onlyFavorites = false
        ))
        val expectedCitiesWithMa = TestUtils.testCities.filter {
            it.fullName.startsWith("ma", ignoreCase = true)
        }.sortedBy { it.fullName }

        assertEquals(expectedCitiesWithMa.size, resultMa.size)
        expectedCitiesWithMa.forEachIndexed { index, city ->
            assertEquals(city.name, resultMa[index].name)
            assertEquals(city.country, resultMa[index].country)
        }
    }

    @Test
    fun `getPaginatedCities filters only favorites`() = runBlocking {
        // Data is already inserted in @Before via TestUtils.testCities

        // Mark some cities as favorites explicitly in the database if your TestUtils.testCities
        // doesn't have them marked (although my placeholder does).
        // For example, if you want "Hurzuf" (id=707860) to be a favorite for this test:
        cityDao.updateCity(TestUtils.testCities.first { it.id == 707860L }.copy(isFavorite = true).toEntity())
        cityDao.updateCity(TestUtils.testCities.first { it.id == 519188L }.copy(isFavorite = true).toEntity()) // Novinki

        val favorites = awaitFlowFirst(cityDao.getPaginatedCities(
            limit = TestUtils.testCities.size, offset = 0, searchQuery = null, onlyFavorites = true
        ))

        // Adjust expected results based on cities in TestUtils.testCities that are favorite,
        // plus any you explicitly marked above.
        // Based on the provided data, and marking Hurzuf and Novinki as favorite for this test:
        val expectedFavorites = TestUtils.testCities.filter { it.isFavorite } +
                TestUtils.testCities.first { it.id == 707860L }.copy(isFavorite = true) +
                TestUtils.testCities.first { it.id == 519188L }.copy(isFavorite = true)

        val distinctSortedFavorites = expectedFavorites
            .distinctBy { it.id } // Handle duplicates if some are already favorite
            .sortedBy { it.fullName }

        assertEquals(distinctSortedFavorites.size, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
        distinctSortedFavorites.forEachIndexed { index, city ->
            assertEquals(city.name, favorites[index].name)
            assertEquals(city.country, favorites[index].country)
        }
    }
}