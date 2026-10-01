package com.example.dogapp.core.domain.breed

import kotlin.jvm.JvmInline

@JvmInline
value class BreedSourceId(
    val value: String,
)

data class MeasureRange(
    val min: Double,
    val max: Double,
)

enum class BreedTrait {
    GoodWithChildren,
    GoodWithOtherDogs,
    GoodWithStrangers,
    Shedding,
    Grooming,
    Drooling,
    CoatLength,
    Playfulness,
    Protectiveness,
    Trainability,
    Energy,
    Barking,
}

data class SourcedBreed(
    val source: BreedSourceId,
    val normalizedName: String,
    val displayName: String,
    val sourceReference: String? = null,
    val weightKg: MeasureRange? = null,
    val heightCm: MeasureRange? = null,
    val lifeSpanYears: MeasureRange? = null,
    val temperament: List<String> = emptyList(),
    val breedGroup: String? = null,
    val origin: String? = null,
    val description: String? = null,
    val history: String? = null,
    val traits: Map<BreedTrait, Int> = emptyMap(),
    val imageUrls: List<String> = emptyList(),
)
