package com.example.dogapp.core.data.breed.local

import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.SourcedBreed

fun SourcedBreed.toEntity(): SourcedBreedEntity =
    SourcedBreedEntity(
        source = source.value,
        normalizedName = normalizedName,
        displayName = displayName,
        weightKgMin = weightKg?.min,
        weightKgMax = weightKg?.max,
        heightCmMin = heightCm?.min,
        heightCmMax = heightCm?.max,
        lifeSpanYearsMin = lifeSpanYears?.min,
        lifeSpanYearsMax = lifeSpanYears?.max,
        temperament = temperament,
        breedGroup = breedGroup,
        origin = origin,
        description = description,
        history = history,
        traits = traits.mapKeys { (trait, _) -> trait.name },
        imageUrls = imageUrls,
    )

fun SourcedBreedEntity.toSourcedBreed(): SourcedBreed =
    SourcedBreed(
        source = BreedSourceId(source),
        normalizedName = normalizedName,
        displayName = displayName,
        weightKg = range(weightKgMin, weightKgMax),
        heightCm = range(heightCmMin, heightCmMax),
        lifeSpanYears = range(lifeSpanYearsMin, lifeSpanYearsMax),
        temperament = temperament,
        breedGroup = breedGroup,
        origin = origin,
        description = description,
        history = history,
        traits = traits.mapNotNull { (name, rating) -> BreedTrait.entries.firstOrNull { it.name == name }?.let { it to rating } }.toMap(),
        imageUrls = imageUrls,
    )

private fun range(
    min: Double?,
    max: Double?,
): MeasureRange? = if (min != null && max != null) MeasureRange(min, max) else null
