package com.example.dogapp.testing

import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSourceId

fun testBreed(
    id: String,
    name: String = id.split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) },
    breedGroup: String? = null,
    imageUrls: List<String> = emptyList(),
) = Breed(
    id = id,
    name = name,
    weightKg = null,
    heightCm = null,
    lifeSpanYears = null,
    temperament = emptyList(),
    breedGroup = breedGroup,
    origin = null,
    description = null,
    history = null,
    traits = emptyMap(),
    imageUrls = imageUrls,
    sources = listOf(BreedSourceId("test")),
    sourceNames = listOf("Test"),
    sourceReferences = emptyMap(),
)
