package com.example.dogapp.core.domain.breed

data class Breed(
    val id: String,
    val name: String,
    val weightKg: MeasureRange?,
    val heightCm: MeasureRange?,
    val lifeSpanYears: MeasureRange?,
    val temperament: List<String>,
    val breedGroup: String?,
    val origin: String?,
    val description: String?,
    val history: String?,
    val traits: Map<BreedTrait, Int>,
    val imageUrls: List<String>,
    val sources: List<BreedSourceId>,
)
