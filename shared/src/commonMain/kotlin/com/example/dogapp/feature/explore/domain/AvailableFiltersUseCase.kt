package com.example.dogapp.feature.explore.domain

import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedTrait

class AvailableFiltersUseCase {
    operator fun invoke(breeds: List<Breed>): Set<FilterCategory> =
        FilterCategory.entries
            .filter { category -> breeds.any { it.hasInfoFor(category) } }
            .toSet()

    private fun Breed.hasInfoFor(category: FilterCategory): Boolean =
        when (category) {
            FilterCategory.Size -> weightKg != null
            FilterCategory.LifeSpan -> lifeSpanYears != null
            FilterCategory.Group -> breedGroup != null
            FilterCategory.Energy -> BreedTrait.Energy in traits
            FilterCategory.Traits -> TraitFilter.entries.any { it.rating in traits }
        }
}
