package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncBreedsUseCaseTest {
    private class FakeRepository(
        private val result: BreedSyncResult,
    ) : BreedRepository {
        override suspend fun syncBreeds() = result

        override fun observeBreeds(): Flow<List<Breed>> = emptyFlow()

        override suspend fun fetchGallery(breed: Breed) = Outcome.Success(emptyList<String>())
    }

    @Test
    fun succeedsWhileAtLeastOneSourceSucceedsAndFailsOnlyWhenEverySourceFailed() =
        runTest {
            val partial =
                BreedSyncResult(
                    succeededSources = setOf(BreedSourceId("a")),
                    failures = listOf(BreedSourceFailure(BreedSourceId("b"), "B", DataError.Remote.NoConnection)),
                )
            val allFailed =
                BreedSyncResult(
                    succeededSources = emptySet(),
                    failures =
                        listOf(
                            BreedSourceFailure(BreedSourceId("a"), "A", DataError.Remote.Unauthorized),
                            BreedSourceFailure(BreedSourceId("b"), "B", DataError.Remote.NoConnection),
                        ),
                )
            val noSources = BreedSyncResult(succeededSources = emptySet(), failures = emptyList())

            assertEquals(Outcome.Success(partial), SyncBreedsUseCase(FakeRepository(partial))())
            assertEquals(Outcome.Failure(DataError.Remote.Unauthorized), SyncBreedsUseCase(FakeRepository(allFailed))())
            assertEquals(Outcome.Success(noSources), SyncBreedsUseCase(FakeRepository(noSources))())
        }
}
