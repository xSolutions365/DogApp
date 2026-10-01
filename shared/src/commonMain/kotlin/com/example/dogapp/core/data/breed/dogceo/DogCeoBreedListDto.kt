package com.example.dogapp.core.data.breed.dogceo

import kotlinx.serialization.Serializable

@Serializable
data class DogCeoBreedListDto(
    val message: Map<String, List<String>>,
    val status: String,
)
