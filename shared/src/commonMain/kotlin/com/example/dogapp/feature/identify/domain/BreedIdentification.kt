package com.example.dogapp.feature.identify.domain

import com.example.dogapp.core.domain.breed.Breed

sealed interface BreedIdentification {
    data class Confident(
        val match: IdentifiedBreed,
    ) : BreedIdentification

    data class Uncertain(
        val candidates: List<IdentifiedBreed>,
    ) : BreedIdentification

    data object NoDog : BreedIdentification
}

data class IdentifiedBreed(
    val name: String,
    val confidence: Float,
    val breed: Breed?,
)
