package com.example.dogapp.core.data.breed.local

import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.SourcedBreed
import kotlin.test.Test
import kotlin.test.assertEquals

class SourcedBreedEntityMappingTest {
    @Test
    fun sourcedBreedsRoundTripThroughTheEntityIncludingAbsentValues() {
        val complete =
            SourcedBreed(
                source = BreedSourceId("the-dog-api"),
                normalizedName = "border collie",
                displayName = "Border Collie",
                weightKg = MeasureRange(14.0, 25.0),
                heightCm = MeasureRange(46.0, 56.0),
                lifeSpanYears = MeasureRange(12.0, 15.0),
                temperament = listOf("Intelligent", "energetic"),
                breedGroup = "Herding",
                origin = "Anglo-Scottish border region",
                description = "Herding dog.",
                history = "Bred by shepherds.",
                traits = BreedTrait.entries.associateWith { it.ordinal % 5 + 1 },
                imageUrls = listOf("https://cdn2.thedogapi.com/images/38KoA9gGB.jpg"),
            )
        val sparse = SourcedBreed(source = BreedSourceId("dog-ceo"), normalizedName = "beagle", displayName = "Beagle")

        assertEquals(complete, complete.toEntity().toSourcedBreed())
        assertEquals(sparse, sparse.toEntity().toSourcedBreed())
    }

    @Test
    fun listAndMapColumnsRoundTripThroughTheTypeConverters() {
        val converters = BreedTypeConverters()
        val list = listOf("Intelligent", "eager to please", "")
        val map = mapOf("Energy" to 5, "Drooling" to 1)

        assertEquals(list, converters.toStringList(converters.fromStringList(list)))
        assertEquals(map, converters.toIntMap(converters.fromIntMap(map)))
        assertEquals(emptyList(), converters.toStringList(converters.fromStringList(emptyList())))
    }
}
