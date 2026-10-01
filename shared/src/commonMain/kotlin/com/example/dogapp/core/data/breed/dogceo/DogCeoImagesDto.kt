package com.example.dogapp.core.data.breed.dogceo

import kotlinx.serialization.Serializable

@Serializable
data class DogCeoImagesDto(
    val message: List<String>,
    val status: String,
)
