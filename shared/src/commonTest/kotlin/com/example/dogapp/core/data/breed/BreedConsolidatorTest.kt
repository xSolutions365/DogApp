package com.example.dogapp.core.data.breed

import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.SourcedBreed
import kotlin.test.Test
import kotlin.test.assertEquals

class BreedConsolidatorTest {
    private val primary = BreedSourceId("primary")
    private val secondary = BreedSourceId("secondary")
    private val tertiary = BreedSourceId("tertiary")
    private val unranked = BreedSourceId("unranked")
    private val consolidator =
        BreedConsolidator(
            precedence = listOf(primary, secondary, tertiary),
            sourceNames = mapOf(primary to "Primary API", secondary to "Secondary API", tertiary to "Tertiary API"),
        )

    @Test
    fun recordsWithTheSameNormalizedNameMergeFieldByFieldInPrecedenceOrder() {
        val records =
            listOf(
                SourcedBreed(
                    source = tertiary,
                    normalizedName = "border collie",
                    displayName = "Border Collie (tertiary)",
                    sourceReference = "collie/border",
                    breedGroup = "Tertiary group",
                    imageUrls = listOf("https://c/1.jpg", "https://shared.jpg"),
                ),
                SourcedBreed(
                    source = secondary,
                    normalizedName = "border collie",
                    displayName = "Border Collie (secondary)",
                    weightKg = MeasureRange(13.6, 24.9),
                    heightCm = MeasureRange(48.3, 55.9),
                    lifeSpanYears = MeasureRange(11.0, 16.0),
                    temperament = listOf("Secondary temperament"),
                    traits = mapOf(BreedTrait.Energy to 4, BreedTrait.Trainability to 5),
                    imageUrls = listOf("https://b/1.jpg", "https://shared.jpg"),
                ),
                SourcedBreed(
                    source = primary,
                    normalizedName = "border collie",
                    displayName = "Border Collie",
                    sourceReference = "50",
                    weightKg = MeasureRange(14.0, 25.0),
                    lifeSpanYears = MeasureRange(12.0, 15.0),
                    origin = "Anglo-Scottish border region",
                    description = "Herding dog.",
                    traits = mapOf(BreedTrait.Energy to 5),
                    imageUrls = listOf("https://a/1.jpg"),
                ),
            )

        val breed = consolidator.consolidate(records).single()

        assertEquals("border collie", breed.id)
        assertEquals("Border Collie", breed.name)
        assertEquals(MeasureRange(14.0, 25.0), breed.weightKg)
        assertEquals(MeasureRange(48.3, 55.9), breed.heightCm)
        assertEquals(MeasureRange(12.0, 15.0), breed.lifeSpanYears)
        assertEquals(listOf("Secondary temperament"), breed.temperament)
        assertEquals("Tertiary group", breed.breedGroup)
        assertEquals("Anglo-Scottish border region", breed.origin)
        assertEquals("Herding dog.", breed.description)
        assertEquals(null, breed.history)
        assertEquals(mapOf(BreedTrait.Energy to 5, BreedTrait.Trainability to 5), breed.traits)
        assertEquals(listOf("https://a/1.jpg", "https://b/1.jpg", "https://shared.jpg", "https://c/1.jpg"), breed.imageUrls)
        assertEquals(listOf(primary, secondary, tertiary), breed.sources)
        assertEquals(listOf("Primary API", "Secondary API", "Tertiary API"), breed.sourceNames)
        assertEquals(mapOf(primary to "50", tertiary to "collie/border"), breed.sourceReferences)
    }

    @Test
    fun distinctBreedsStaySeparateSortedByNameAndUnrankedSourcesComeLast() {
        val records =
            listOf(
                SourcedBreed(source = unranked, normalizedName = "beagle", displayName = "Beagle (unranked)", origin = "Unranked"),
                SourcedBreed(source = tertiary, normalizedName = "beagle", displayName = "Beagle", origin = "England"),
                SourcedBreed(source = primary, normalizedName = "akita", displayName = "Akita"),
            )

        val breeds = consolidator.consolidate(records)

        assertEquals(listOf("Akita", "Beagle"), breeds.map { it.name })
        assertEquals("England", breeds.single { it.id == "beagle" }.origin)
        assertEquals(listOf(tertiary, unranked), breeds.single { it.id == "beagle" }.sources)
        assertEquals(listOf("Tertiary API", "unranked"), breeds.single { it.id == "beagle" }.sourceNames)
    }
}
