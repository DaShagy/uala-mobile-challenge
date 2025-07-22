package com.juanjoseabuin.ualacitymobilechallenge.domain.utils

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.City

object SearchCityUtils {

    /**
     * Searches a list of [City] objects whose `fullName` starts with the given prefix.
     * Assumes the input list is ALREADY SORTED by fullName (name then country).
     *
     * @param prefix The prefix to search for.
     * @return A new list of filtered cities.
     *
     * Time Complexity: O(N * P)
     * - N: Number of cities in the list (210,000).
     * - P: Length of the `prefix`.
     * - This is a linear scan, performing `startsWith` for each element.
     *
     * Space Complexity: O(K) where K is the number of matching cities.
     * - A new list is created to store the filtered results. In the worst case (all match), K=N, so O(N).
     */
    fun List<City>.searchByPrefix(prefix: String = ""): List<City> {
        // No sorting or order check needed, just filter
        return this.filter { city -> city.fullName.startsWith(prefix, ignoreCase = true) }
    }
}