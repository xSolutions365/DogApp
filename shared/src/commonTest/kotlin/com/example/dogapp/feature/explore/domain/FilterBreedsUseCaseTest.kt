package com.example.dogapp.feature.explore.domain

import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.testing.testBreed
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterBreedsUseCaseTest {
    private val filterBreeds = FilterBreedsUseCase()

    private fun ids(
        filters: BreedFilters,
        favouriteIds: Set<String> = emptySet(),
        vararg breeds: Breed,
    ) = filterBreeds(breeds.toList(), filters, favouriteIds).breeds.map { it.id }

    @Test
    fun withoutFiltersEveryBreedIsShownAndNothingIsHidden() {
        val breeds = listOf(testBreed("akita"), testBreed("beagle", weightKg = MeasureRange(9.0, 11.0)))

        assertEquals(FilteredBreeds(breeds, emptyMap()), filterBreeds(breeds, BreedFilters(), emptySet()))
    }

    @Test
    fun eachBreedHasOneSizeFromTheMidpointOfItsWeightRange() {
        val breeds =
            arrayOf(
                testBreed("chihuahua", weightKg = MeasureRange(1.5, 3.0)),
                testBreed("pug", weightKg = MeasureRange(4.0, 6.0)),
                testBreed("beagle", weightKg = MeasureRange(9.0, 13.0)),
                testBreed("labrador", weightKg = MeasureRange(25.0, 36.0)),
                testBreed("mastiff", weightKg = MeasureRange(54.0, 100.0)),
            )

        assertEquals(listOf("chihuahua"), ids(BreedFilters(sizes = setOf(BreedSize.Toy)), breeds = breeds))
        assertEquals(listOf("pug"), ids(BreedFilters(sizes = setOf(BreedSize.Small)), breeds = breeds))
        assertEquals(listOf("beagle"), ids(BreedFilters(sizes = setOf(BreedSize.Medium)), breeds = breeds))
        assertEquals(listOf("labrador"), ids(BreedFilters(sizes = setOf(BreedSize.Large)), breeds = breeds))
        assertEquals(listOf("mastiff"), ids(BreedFilters(sizes = setOf(BreedSize.Giant)), breeds = breeds))
        assertEquals(
            listOf("pug", "labrador"),
            ids(BreedFilters(sizes = setOf(BreedSize.Small, BreedSize.Large)), breeds = breeds),
        )
    }

    @Test
    fun lifeSpanBandsComeFromTheMidpointOfTheLifeSpanRange() {
        val breeds =
            arrayOf(
                testBreed("great dane", lifeSpanYears = MeasureRange(7.0, 10.0)),
                testBreed("labrador", lifeSpanYears = MeasureRange(10.0, 12.0)),
                testBreed("collie", lifeSpanYears = MeasureRange(12.0, 14.0)),
            )

        assertEquals(listOf("great dane"), ids(BreedFilters(lifeSpans = setOf(LifeSpanBand.UnderTen)), breeds = breeds))
        assertEquals(listOf("labrador"), ids(BreedFilters(lifeSpans = setOf(LifeSpanBand.TenToTwelve)), breeds = breeds))
        assertEquals(listOf("collie"), ids(BreedFilters(lifeSpans = setOf(LifeSpanBand.ThirteenPlus)), breeds = breeds))
    }

    @Test
    fun groupEnergyAndTraitFiltersMatchTheirRatingsAndAllActiveFiltersMustMatch() {
        val breeds =
            arrayOf(
                testBreed(
                    "border collie",
                    breedGroup = "Herding",
                    traits =
                        mapOf(
                            BreedTrait.Energy to 5,
                            BreedTrait.GoodWithChildren to 4,
                            BreedTrait.Shedding to 3,
                            BreedTrait.Trainability to 5,
                        ),
                ),
                testBreed(
                    "basset hound",
                    breedGroup = "Hound",
                    traits =
                        mapOf(
                            BreedTrait.Energy to 2,
                            BreedTrait.GoodWithChildren to 5,
                            BreedTrait.Shedding to 2,
                            BreedTrait.Trainability to 2,
                        ),
                ),
                testBreed(
                    "bulldog",
                    breedGroup = "Non-Sporting",
                    traits =
                        mapOf(
                            BreedTrait.Energy to 3,
                            BreedTrait.GoodWithChildren to 3,
                            BreedTrait.Shedding to 3,
                            BreedTrait.Trainability to 4,
                        ),
                ),
            )

        assertEquals(listOf("basset hound"), ids(BreedFilters(groups = setOf("Hound")), breeds = breeds))
        assertEquals(listOf("basset hound"), ids(BreedFilters(energyLevels = setOf(EnergyLevel.Low)), breeds = breeds))
        assertEquals(listOf("bulldog"), ids(BreedFilters(energyLevels = setOf(EnergyLevel.Medium)), breeds = breeds))
        assertEquals(listOf("border collie"), ids(BreedFilters(energyLevels = setOf(EnergyLevel.High)), breeds = breeds))
        assertEquals(
            listOf("border collie", "basset hound"),
            ids(BreedFilters(traits = setOf(TraitFilter.GoodWithChildren)), breeds = breeds),
        )
        assertEquals(listOf("basset hound"), ids(BreedFilters(traits = setOf(TraitFilter.LowShedding)), breeds = breeds))
        assertEquals(
            listOf("border collie", "bulldog"),
            ids(BreedFilters(traits = setOf(TraitFilter.EasyToTrain)), breeds = breeds),
        )
        assertEquals(
            listOf("border collie"),
            ids(BreedFilters(traits = setOf(TraitFilter.GoodWithChildren, TraitFilter.EasyToTrain)), breeds = breeds),
        )
        assertEquals(
            emptyList(),
            ids(BreedFilters(groups = setOf("Hound"), energyLevels = setOf(EnergyLevel.High)), breeds = breeds),
        )
    }

    @Test
    fun favouritesOnlyKeepsFavouritesWithoutHidingAnyBreed() {
        val result =
            filterBreeds(
                listOf(testBreed("akita"), testBreed("beagle")),
                BreedFilters(favouritesOnly = true),
                setOf("beagle"),
            )

        assertEquals(listOf("beagle"), result.breeds.map { it.id })
        assertEquals(emptyMap(), result.hiddenForMissingInfo)
    }

    @Test
    fun breedsMissingFilteredInfoAreHiddenAndCountedOnlyWhenNoOtherFilterRulesThemOut() {
        val breeds =
            listOf(
                testBreed("beagle", breedGroup = "Hound", weightKg = MeasureRange(9.0, 13.0)),
                testBreed("bloodhound", breedGroup = "Hound"),
                testBreed("basenji", breedGroup = "Hound", traits = mapOf(BreedTrait.Energy to 4)),
                testBreed("unknown hound"),
                testBreed("collie", breedGroup = "Herding"),
                testBreed("husky", weightKg = MeasureRange(16.0, 27.0), traits = mapOf(BreedTrait.Energy to 5)),
            )

        val result =
            filterBreeds(
                breeds,
                BreedFilters(
                    sizes = setOf(BreedSize.Medium),
                    groups = setOf("Hound"),
                    energyLevels = setOf(EnergyLevel.High),
                ),
                emptySet(),
            )

        assertEquals(emptyList(), result.breeds.map { it.id })
        assertEquals(
            mapOf(BreedInfo.Size to 3, BreedInfo.Group to 2, BreedInfo.Traits to 3),
            result.hiddenForMissingInfo,
        )

        val withoutSize = filterBreeds(breeds, BreedFilters(groups = setOf("Hound")), emptySet())
        assertEquals(listOf("beagle", "bloodhound", "basenji"), withoutSize.breeds.map { it.id })
        assertEquals(mapOf(BreedInfo.Group to 2), withoutSize.hiddenForMissingInfo)
    }
}
