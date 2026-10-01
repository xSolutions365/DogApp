package com.example.dogapp.core.data.breed

import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.SourcedBreed

class BreedConsolidator(
    private val precedence: List<BreedSourceId>,
    private val sourceNames: Map<BreedSourceId, String>,
) {
    fun consolidate(records: List<SourcedBreed>): List<Breed> =
        records
            .groupBy { it.normalizedName }
            .map { (normalizedName, group) -> merge(normalizedName, group.sortedBy { rank(it.source) }) }
            .sortedBy { it.name.lowercase() }

    private fun rank(source: BreedSourceId): Int = precedence.indexOf(source).takeIf { it >= 0 } ?: precedence.size

    private fun merge(
        normalizedName: String,
        ordered: List<SourcedBreed>,
    ): Breed =
        Breed(
            id = normalizedName,
            name = ordered.first().displayName,
            weightKg = ordered.firstNotNullOfOrNull { it.weightKg },
            heightCm = ordered.firstNotNullOfOrNull { it.heightCm },
            lifeSpanYears = ordered.firstNotNullOfOrNull { it.lifeSpanYears },
            temperament = ordered.firstOrNull { it.temperament.isNotEmpty() }?.temperament.orEmpty(),
            breedGroup = ordered.firstNotNullOfOrNull { it.breedGroup },
            origin = ordered.firstNotNullOfOrNull { it.origin },
            description = ordered.firstNotNullOfOrNull { it.description },
            history = ordered.firstNotNullOfOrNull { it.history },
            traits = ordered.asReversed().fold(emptyMap<BreedTrait, Int>()) { traits, record -> traits + record.traits },
            imageUrls = ordered.flatMap { it.imageUrls }.distinct(),
            sources = ordered.map { it.source },
            sourceNames = ordered.map { sourceNames[it.source] ?: it.source.value },
            sourceReferences = ordered.mapNotNull { record -> record.sourceReference?.let { record.source to it } }.toMap(),
        )
}
