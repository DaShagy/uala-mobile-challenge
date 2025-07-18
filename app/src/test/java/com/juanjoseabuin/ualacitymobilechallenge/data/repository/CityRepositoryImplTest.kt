package com.juanjoseabuin.ualacitymobilechallenge.data.repository

import com.juanjoseabuin.ualacitymobilechallenge.data.source.CityJsonDataSource
import com.juanjoseabuin.ualacitymobilechallenge.data.utils.TestUtils
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class CityRepositoryImplTest {

    private lateinit var cityRepository: CityRepositoryImpl

    private val mockLocalDataSource: CityJsonDataSource = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val testCities: List<City> by lazy {
        TestUtils.readJsonResource<List<City>>("test_cities.json").sortedBy { it.name }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { mockLocalDataSource.getCities() } returns Result.success(testCities)

        cityRepository = CityRepositoryImpl(mockLocalDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCities calls local data source getCities once and caches the result`() = runTest {

        val firstResult = cityRepository.getCities()

        coVerify(exactly = 1) { mockLocalDataSource.getCities() }
        assertTrue(firstResult.isSuccess)
        assertEquals(testCities, firstResult.getOrNull())

        val secondResult = cityRepository.getCities()

        coVerify(exactly = 1) { mockLocalDataSource.getCities() }
        assertTrue(secondResult.isSuccess)
        assertEquals(testCities, secondResult.getOrNull())
    }

    @Test
    fun `getCities returns cities sorted by name`() = runTest {

        val result = cityRepository.getCities()

        assertTrue(result.isSuccess)
        val cities = result.getOrNull()
        assertNotNull(cities)

        assertTrue("The list of cities should be sorted by name",
            cities!!.zipWithNext { a, b -> a.name <= b.name }.all { it }
        )

        assertEquals(testCities.size, cities.size) // Check size
        assertTrue(cities.containsAll(testCities)) // Check if all original elements are there
        assertTrue(testCities.containsAll(cities)) // Check no extra elements
    }
    @Test
    fun `searchCities returns matching cities by prefix (case-insensitive)`() = runTest {
        val result = cityRepository.searchCities("a")

        assertTrue(result.isSuccess)
        val cities = result.getOrNull()
        assertNotNull(cities)

        val expectedCities = testCities.filter {
            it.name.startsWith("a", ignoreCase = true)
        }

        assertEquals(expectedCities.size, cities?.size)
        assertEquals(expectedCities, cities)
    }

    @Test
    fun `searchCities does not return cities that do not match the prefix`() = runTest {

        // Act: Search for prefixes that are NOT in test_cities.json.
        val resultNonExistent1 = cityRepository.searchCities("London")
        val resultNonExistent2 = cityRepository.searchCities("Paris")

        // Assert for non-existent cities: should return empty list.
        assertTrue(resultNonExistent1.isSuccess)
        assertTrue(resultNonExistent1.getOrNull().isNullOrEmpty())

        assertTrue(resultNonExistent2.isSuccess)
        assertTrue(resultNonExistent2.getOrNull().isNullOrEmpty())

        // Act: Search for prefixes that ARE in test_cities.json.
        val resultMar = cityRepository.searchCities("Mar") // Should match "Mar’ina Roshcha"
        val resultGor = cityRepository.searchCities("Gor") // Should match "Gorkhā"

        // Assert for existing cities: verify size and content.
        assertTrue(resultMar.isSuccess)
        val citiesMar = resultMar.getOrDefault(listOf())
        assertEquals(1, citiesMar.size)
        assertEquals(testCities.first { it.name == "Mar’ina Roshcha" }, citiesMar.first())

        assertTrue(resultGor.isSuccess)
        val citiesGor = resultGor.getOrDefault(listOf())
        assertEquals(1, citiesGor.size)
        assertEquals(testCities.first { it.name == "Gorkhā" }, citiesGor.first())
    }

    @Test
    fun `searchCities returns empty list when no matches`() = runTest {
        // Arrange: The repository will load 'testCities'.

        // Act: Search for a prefix that is highly unlikely to match any city.
        val result = cityRepository.searchCities("NonExistentCityName")

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun `searchCities returns correct matches for 'hol'`() = runTest {
        // Arrange: The repository will load 'testCities'.

        val result = cityRepository.searchCities("hol")

        // Assert
        assertTrue(result.isSuccess)
        val cities = result.getOrNull()
        assertNotNull(cities)

        val expectedCities = testCities.filter { it.name.startsWith("hol", ignoreCase = true) }
            .sortedBy { it.name }

        assertEquals(expectedCities.size, cities?.size)
        assertEquals(expectedCities, cities)
    }

    @Test
    fun `searchCities handles case sensitivity correctly`() = runTest {
        // Arrange: The repository will load 'testCities'.
        // Assuming your search logic (in CityRepositoryImpl) is case-insensitive.

        // Act: Search for "moskva" (lowercase) and "Moskva" (uppercase)
        val resultLower = cityRepository.searchCities("moskva")
        val resultUpper = cityRepository.searchCities("Moskva")

        // Assert that both yield the same successful result
        assertTrue(resultLower.isSuccess)
        assertTrue(resultUpper.isSuccess)

        val citiesLower = resultLower.getOrNull()
        val citiesUpper = resultUpper.getOrNull()

        // Assert that both results contain "Moskva" and are identical.
        assertEquals(1, citiesLower?.size)
        assertEquals(1, citiesUpper?.size)
        assertEquals(citiesLower, citiesUpper)
        assertEquals(testCities.first { it.name == "Moskva" }, citiesLower?.first())
    }

    @Test
    fun `getCities handles local data source failure`() = runTest {
        val failureException = RuntimeException("Simulated local data source error during getCities")
        coEvery { mockLocalDataSource.getCities() } returns Result.failure(failureException)

        val result = cityRepository.getCities()

        assertTrue(result.isFailure)
        assertEquals(failureException, result.exceptionOrNull())

        coVerify(exactly = 1) { mockLocalDataSource.getCities() }
    }

    @Test
    fun `searchCities propagates failure from getCities`() = runTest {

        val failureException = RuntimeException("Simulated local data source error during searchCities setup")
        coEvery { mockLocalDataSource.getCities() } returns Result.failure(failureException)

        val result = cityRepository.searchCities("any_prefix")

        assertTrue(result.isFailure)
        assertEquals(failureException, result.exceptionOrNull())

        coVerify(exactly = 1) { mockLocalDataSource.getCities() }
    }
}