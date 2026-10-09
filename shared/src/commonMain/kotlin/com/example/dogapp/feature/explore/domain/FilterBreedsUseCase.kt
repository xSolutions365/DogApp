package com.example.dogapp.feature.explore.domain

import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange

data class FilteredBreeds(
    val breeds: List<Breed>,
    val hiddenForMissingInfo: Map<BreedInfo, Int>,
)

class FilterBreedsUseCase {
    operator fun invoke(
        breeds: List<Breed>,
        filters: BreedFilters,
        favouriteIds: Set<String>,
    ): FilteredBreeds {
        val shown = mutableListOf<Breed>()
        val hidden = mutableMapOf<BreedInfo, Int>()
        breeds
            .filter { !filters.favouritesOnly || it.id in favouriteIds }
            .forEach { breed ->
                val checks = checks(breed, filters)
                when {
                    checks.any { it.value == Check.Fails } -> {
                        Unit
                    }

                    checks.any { it.value == Check.Unknown } -> {
                        checks.filterValues { it == Check.Unknown }.keys.forEach { hidden[it] = (hidden[it] ?: 0) + 1 }
                    }

                    else -> {
                        shown += breed
                    }
                }
            }
        return FilteredBreeds(shown, hidden)
    }

    private enum class Check { Passes, Fails, Unknown }

    private fun checks(
        breed: Breed,
        filters: BreedFilters,
    ): Map<BreedInfo, Check> =
        buildMap {
            if (filters.sizes.isNotEmpty()) {
                put(BreedInfo.Size, check(breed.weightKg?.let(::sizeOf)) { it in filters.sizes })
            }
            if (filters.lifeSpans.isNotEmpty()) {
                put(BreedInfo.LifeSpan, check(breed.lifeSpanYears?.let(::lifeSpanBandOf)) { it in filters.lifeSpans })
            }
            if (filters.groups.isNotEmpty()) {
                put(BreedInfo.Group, check(breed.breedGroup) { it in filters.groups })
            }
            val traitChecks =
                buildList {
                    if (filters.energyLevels.isNotEmpty()) {
                        add(check(breed.traits[BreedTrait.Energy]?.let(::energyLevelOf)) { it in filters.energyLevels })
                    }
                    filters.traits.forEach { trait -> add(traitCheck(breed, trait)) }
                }
            if (traitChecks.isNotEmpty()) {
                put(
                    BreedInfo.Traits,
                    when {
                        Check.Fails in traitChecks -> Check.Fails
                        Check.Unknown in traitChecks -> Check.Unknown
                        else -> Check.Passes
                    },
                )
            }
        }

    private fun traitCheck(
        breed: Breed,
        trait: TraitFilter,
    ): Check =
        check(breed.traits[trait.rating]) { rating ->
            when (trait) {
                TraitFilter.GoodWithChildren, TraitFilter.EasyToTrain -> rating >= HIGH_RATING
                TraitFilter.LowShedding -> rating <= LOW_RATING
            }
        }

    private fun <T : Any> check(
        value: T?,
        passes: (T) -> Boolean,
    ): Check =
        when {
            value == null -> Check.Unknown
            passes(value) -> Check.Passes
            else -> Check.Fails
        }

    private fun sizeOf(weightKg: MeasureRange): BreedSize {
        val midpoint = weightKg.midpoint
        return when {
            midpoint < SMALL_MIN_KG -> BreedSize.Toy
            midpoint < MEDIUM_MIN_KG -> BreedSize.Small
            midpoint < LARGE_MIN_KG -> BreedSize.Medium
            midpoint < GIANT_MIN_KG -> BreedSize.Large
            else -> BreedSize.Giant
        }
    }

    private fun lifeSpanBandOf(lifeSpanYears: MeasureRange): LifeSpanBand {
        val midpoint = lifeSpanYears.midpoint
        return when {
            midpoint < TEN_YEARS -> LifeSpanBand.UnderTen
            midpoint < THIRTEEN_YEARS -> LifeSpanBand.TenToTwelve
            else -> LifeSpanBand.ThirteenPlus
        }
    }

    private fun energyLevelOf(rating: Int): EnergyLevel =
        when {
            rating <= LOW_RATING -> EnergyLevel.Low
            rating < HIGH_RATING -> EnergyLevel.Medium
            else -> EnergyLevel.High
        }

    private val MeasureRange.midpoint: Double get() = (min + max) / 2

    private companion object {
        const val SMALL_MIN_KG = 5.0
        const val MEDIUM_MIN_KG = 11.0
        const val LARGE_MIN_KG = 25.0
        const val GIANT_MIN_KG = 45.0
        const val TEN_YEARS = 10.0
        const val THIRTEEN_YEARS = 13.0
        const val LOW_RATING = 2
        const val HIGH_RATING = 4
    }
}
