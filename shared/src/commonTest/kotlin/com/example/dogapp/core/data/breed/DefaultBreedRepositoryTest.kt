package com.example.dogapp.core.data.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.SourcedBreed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultBreedRepositoryTest {
    private class FakeSource(
        name: String,
        private val outcome: suspend () -> Outcome<List<SourcedBreed>, DataError.Remote>,
    ) : BreedSource {
        override val id = BreedSourceId(name)

        override suspend fun fetchBreeds() = outcome()
    }

    private fun breed(
        source: String,
        name: String,
    ) = SourcedBreed(source = BreedSourceId(source), normalizedName = name, displayName = name)

    @Test
    fun breedsFromSucceedingSourcesAreCombinedAndFailuresReportedPerSource() =
        runTest {
            val repository =
                DefaultBreedRepository(
                    listOf(
                        FakeSource("a") { Outcome.Success(listOf(breed("a", "beagle"))) },
                        FakeSource("b") { Outcome.Failure(DataError.Remote.Unauthorized) },
                        FakeSource("c") { Outcome.Success(listOf(breed("c", "border collie"), breed("c", "beagle"))) },
                    ),
                )

            val result = repository.fetchBreeds()

            assertEquals(listOf(breed("a", "beagle"), breed("c", "border collie"), breed("c", "beagle")), result.breeds)
            assertEquals(setOf(BreedSourceId("a"), BreedSourceId("c")), result.succeededSources)
            assertEquals(mapOf(BreedSourceId("b") to DataError.Remote.Unauthorized), result.failures)
        }

    @Test
    fun sourcesAreFetchedConcurrently() =
        runTest {
            val firstStarted = CompletableDeferred<Unit>()
            val secondStarted = CompletableDeferred<Unit>()
            val repository =
                DefaultBreedRepository(
                    listOf(
                        FakeSource("a") {
                            firstStarted.complete(Unit)
                            secondStarted.await()
                            Outcome.Success(emptyList())
                        },
                        FakeSource("b") {
                            secondStarted.complete(Unit)
                            firstStarted.await()
                            Outcome.Success(emptyList())
                        },
                    ),
                )

            val result = repository.fetchBreeds()

            assertEquals(setOf(BreedSourceId("a"), BreedSourceId("b")), result.succeededSources)
        }
}
