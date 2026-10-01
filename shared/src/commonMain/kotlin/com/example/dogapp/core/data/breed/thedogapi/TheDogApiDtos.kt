package com.example.dogapp.core.data.breed.thedogapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TheDogApiBreedDto(
    val id: String,
    val name: String,
    @SerialName("life_span") val lifeSpan: String? = null,
    val temperament: String? = null,
    val origin: String? = null,
    @SerialName("breed_group") val breedGroup: String? = null,
    val description: String? = null,
    val history: String? = null,
    val weight: TheDogApiMeasurementDto? = null,
    val height: TheDogApiMeasurementDto? = null,
    val image: TheDogApiImageDto? = null,
)

@Serializable
data class TheDogApiMeasurementDto(
    val metric: String? = null,
)

@Serializable
data class TheDogApiImageDto(
    val url: String,
)

@Serializable
data class TheDogApiSearchImageDto(
    val url: String,
)
