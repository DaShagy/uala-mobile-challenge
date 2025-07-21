package com.juanjoseabuin.ualacitymobilechallenge.data.source.remote.response

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Country
import com.juanjoseabuin.ualacitymobilechallenge.domain.model.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CountryApiResponse(
    @SerialName("gdp") val gdp: Double?,
    @SerialName("sex_ratio") val sexRatio: Double?,
    @SerialName("surface_area") val surfaceArea: Double?,
    @SerialName("life_expectancy_male") val lifeExpectancyMale: Double?,
    @SerialName("unemployment") val unemployment: Double?,
    @SerialName("imports") val imports: Double?,
    @SerialName("currency") val currency: CurrencyApiResponse?,
    @SerialName("homicide_rate") val homicideRate: Double?,
    @SerialName("iso2") val iso2: String?,
    @SerialName("gdp_growth") val gdpGrowth: Double?,
    @SerialName("employment_services") val employmentServices: Double?,
    @SerialName("urban_population_growth") val urbanPopulationGrowth: Double?,
    @SerialName("secondary_school_enrollment_female") val secondarySchoolEnrollmentFemale: Double?,
    @SerialName("capital") val capital: String?,
    @SerialName("employment_agriculture") val employmentAgriculture: Double?,
    @SerialName("co2_emissions") val co2Emissions: Double?,
    @SerialName("forested_area") val forestedArea: Double?,
    @SerialName("tourists") val tourists: Double?,
    @SerialName("exports") val exports: Double?,
    @SerialName("life_expectancy_female") val lifeExpectancyFemale: Double?,
    @SerialName("post_secondary_enrollment_female") val postSecondaryEnrollmentFemale: Double?,
    @SerialName("post_secondary_enrollment_male") val postSecondaryEnrollmentMale: Double?,
    @SerialName("primary_school_enrollment_female") val primarySchoolEnrollmentFemale: Double?,
    @SerialName("infant_mortality") val infantMortality: Double?,
    @SerialName("secondary_school_enrollment_male") val secondarySchoolEnrollmentMale: Double?,
    @SerialName("threatened_species") val threatenedSpecies: Double?,
    @SerialName("population") val population: Double?,
    @SerialName("urban_population") val urbanPopulation: Double?,
    @SerialName("employment_industry") val employmentIndustry: Double?,
    @SerialName("name") val name: String?,
    @SerialName("region") val region: String?,
    @SerialName("pop_growth") val popGrowth: Double?,
    @SerialName("pop_density") val popDensity: Double?,
    @SerialName("internet_users") val internetUsers: Double?,
    @SerialName("gdp_per_capita") val gdpPerCapita: Double?,
    @SerialName("fertility") val fertility: Double?,
    @SerialName("refugees") val refugees: Double?,
    @SerialName("primary_school_enrollment_male") val primarySchoolEnrollmentMale: Double?,
    @SerialName("telephone_country_codes") val telephoneCountryCodes: List<String>?
)

@Serializable
data class CurrencyApiResponse(
    @SerialName("code") val code: String?,
    @SerialName("name") val name: String?
)

fun CountryApiResponse.toDomain(): Country {
    return Country(
        countryCode = this.iso2 ?: "",
        name = this.name ?: "Unknown Country",
        population = this.population?.toLong() ?: 0L,
        region = this.region ?: "Unknown Region",
        currency = this.currency?.toDomain() ?: Currency("N/A", "N/A")
    )
}

fun CurrencyApiResponse.toDomain(): Currency {
    return Currency(
        code = this.code ?: "N/A",
        name = this.name ?: "N/A"
    )
}