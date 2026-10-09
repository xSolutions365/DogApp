package com.example.dogapp.testing

import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange

fun testBreed(
    id: String,
    name: String = id.split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) },
    breedGroup: String? = null,
    imageUrls: List<String> = emptyList(),
    weightKg: MeasureRange? = null,
    lifeSpanYears: MeasureRange? = null,
    traits: Map<BreedTrait, Int> = emptyMap(),
) = Breed(
    id = id,
    name = name,
    weightKg = weightKg,
    heightCm = null,
    lifeSpanYears = lifeSpanYears,
    temperament = emptyList(),
    breedGroup = breedGroup,
    origin = null,
    description = null,
    history = null,
    traits = traits,
    imageUrls = imageUrls,
    sources = listOf(BreedSourceId("test")),
    sourceNames = listOf("Test"),
    sourceReferences = emptyMap(),
)
