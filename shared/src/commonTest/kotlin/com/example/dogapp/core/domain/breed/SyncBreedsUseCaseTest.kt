package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncBreedsUseCaseTest {
    private class FakeRepository(
        private val result: BreedFetchResult,
    ) : BreedRepository {
        override suspend fun fetchBreeds() = result
    }

    private val beagle = SourcedBreed(source = BreedSourceId("a"), normalizedName = "beagle", displayName = "Beagle")

    @Test
    fun succeedsWhileAtLeastOneSourceSucceedsAndFailsOnlyWhenEverySourceFailed() =
        runTest {
            val partial =
                BreedFetchResult(
                    breeds = listOf(beagle),
                    succeededSources = setOf(BreedSourceId("a")),
                    failures = mapOf(BreedSourceId("b") to DataError.Remote.NoConnection),
                )
            val emptyButSucceeded =
                BreedFetchResult(
                    breeds = emptyList(),
                    succeededSources = setOf(BreedSourceId("a")),
                    failures = mapOf(BreedSourceId("b") to DataError.Remote.NoConnection),
                )
            val allFailed =
                BreedFetchResult(
                    breeds = emptyList(),
                    succeededSources = emptySet(),
                    failures =
                        mapOf(
                            BreedSourceId("a") to DataError.Remote.Unauthorized,
                            BreedSourceId("b") to DataError.Remote.NoConnection,
                        ),
                )

            assertEquals(Outcome.Success(partial), SyncBreedsUseCase(FakeRepository(partial))())
            assertEquals(Outcome.Success(emptyButSucceeded), SyncBreedsUseCase(FakeRepository(emptyButSucceeded))())
            assertEquals(Outcome.Failure(DataError.Remote.Unauthorized), SyncBreedsUseCase(FakeRepository(allFailed))())
        }
}
