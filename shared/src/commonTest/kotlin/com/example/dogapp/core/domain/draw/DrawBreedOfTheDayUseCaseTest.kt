package com.example.dogapp.core.domain.draw

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeDailyDrawRepository
import com.example.dogapp.testing.FakeTodayProvider
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DrawBreedOfTheDayUseCaseTest {
    private val draws = FakeDailyDrawRepository()
    private val breeds = FakeBreedRepository()
    private val today = FakeTodayProvider("2026-10-01")
    private val drawBreedOfTheDay = DrawBreedOfTheDayUseCase(draws, breeds, today, Random(seed = 7))

    private suspend fun drawOn(day: String): DailyDraw {
        today.day.value = DrawDay(day)
        return (drawBreedOfTheDay() as Outcome.Success).data
    }

    @Test
    fun aDayKeepsItsFirstDrawAndRecordsItUnderThatDay() =
        runTest {
            breeds.breeds.value = (1..10).map { testBreed("breed $it", imageUrls = listOf("https://$it.jpg")) }

            val first = drawOn("2026-10-01")
            val again = drawOn("2026-10-01")

            assertEquals(first, again)
            assertEquals(DrawDay("2026-10-01"), first.day)
            assertEquals(
                breeds.breeds.value
                    .first { it.id == first.breedId }
                    .name,
                first.name,
            )
            assertEquals(
                listOf(first),
                draws.draws.value.values
                    .toList(),
            )
        }

    @Test
    fun breedsWithPhotosAreDrawnWithoutRepeatsUntilEveryOneHasHadADayThenTheCycleRestarts() =
        runTest {
            val withPhotos = (1..4).map { testBreed("photo $it", imageUrls = listOf("https://$it.jpg")) }
            breeds.breeds.value = withPhotos + testBreed("no photo")

            val firstCycle = (1..4).map { drawOn("2026-10-0$it").breedId }
            val nextCycleStart = drawOn("2026-10-05").breedId

            assertEquals(withPhotos.map { it.id }.toSet(), firstCycle.toSet())
            assertTrue(nextCycleStart in firstCycle)
        }

    @Test
    fun breedsWithoutPhotosAreDrawnWhenNoneHavePhotos() =
        runTest {
            breeds.breeds.value = listOf(testBreed("akita"), testBreed("beagle"))

            val drawn = listOf(drawOn("2026-10-01"), drawOn("2026-10-02")).map { it.breedId }

            assertEquals(setOf("akita", "beagle"), drawn.toSet())
        }

    @Test
    fun nothingIsRecordedWhenThereAreNoBreeds() =
        runTest {
            assertEquals(Outcome.Failure(DrawError.NoBreeds), drawBreedOfTheDay())
            assertEquals(emptyMap(), draws.draws.value)
        }
}
