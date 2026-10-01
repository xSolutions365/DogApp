package com.example.dogapp.core.data.breed.apininjas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiNinjasDogDto(
    val name: String,
    @SerialName("image_link") val imageLink: String? = null,
    @SerialName("good_with_children") val goodWithChildren: Int? = null,
    @SerialName("good_with_other_dogs") val goodWithOtherDogs: Int? = null,
    @SerialName("good_with_strangers") val goodWithStrangers: Int? = null,
    val shedding: Int? = null,
    val grooming: Int? = null,
    val drooling: Int? = null,
    @SerialName("coat_length") val coatLength: Int? = null,
    val playfulness: Int? = null,
    val protectiveness: Int? = null,
    val trainability: Int? = null,
    val energy: Int? = null,
    val barking: Int? = null,
    @SerialName("min_life_expectancy") val minLifeExpectancy: Double? = null,
    @SerialName("max_life_expectancy") val maxLifeExpectancy: Double? = null,
    @SerialName("min_height_male") val minHeightMale: Double? = null,
    @SerialName("max_height_male") val maxHeightMale: Double? = null,
    @SerialName("min_height_female") val minHeightFemale: Double? = null,
    @SerialName("max_height_female") val maxHeightFemale: Double? = null,
    @SerialName("min_weight_male") val minWeightMale: Double? = null,
    @SerialName("max_weight_male") val maxWeightMale: Double? = null,
    @SerialName("min_weight_female") val minWeightFemale: Double? = null,
    @SerialName("max_weight_female") val maxWeightFemale: Double? = null,
)
