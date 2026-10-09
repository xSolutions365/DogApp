package com.example.dogapp.feature.explore.domain

import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.testing.testBreed
import kotlin.test.Test
import kotlin.test.assertEquals

class AvailableFiltersUseCaseTest {
    private val availableFilters = AvailableFiltersUseCase()

    @Test
    fun aFilterIsAvailableOnlyWhenAtLeastOneBreedHasTheInfoItNeeds() {
        assertEquals(emptySet(), availableFilters(emptyList()))
        assertEquals(emptySet(), availableFilters(listOf(testBreed("akita"), testBreed("beagle"))))

        assertEquals(
            setOf(FilterCategory.Size, FilterCategory.Group),
            availableFilters(
                listOf(
                    testBreed("akita", weightKg = MeasureRange(32.0, 59.0)),
                    testBreed("beagle", breedGroup = "Hound"),
                ),
            ),
        )
        assertEquals(
            setOf(FilterCategory.LifeSpan, FilterCategory.Energy),
            availableFilters(
                listOf(
                    testBreed("akita", lifeSpanYears = MeasureRange(10.0, 14.0)),
                    testBreed("beagle", traits = mapOf(BreedTrait.Energy to 4, BreedTrait.Barking to 5)),
                ),
            ),
        )
        assertEquals(
            setOf(FilterCategory.Traits),
            availableFilters(listOf(testBreed("beagle", traits = mapOf(BreedTrait.Shedding to 3)))),
        )
        assertEquals(emptySet(), availableFilters(listOf(testBreed("beagle", traits = mapOf(BreedTrait.Drooling to 1)))))
    }
}
