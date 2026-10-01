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
    }

    @Test
    fun succeedsWhileAtLeastOneSourceSucceedsAndFailsOnlyWhenEverySourceFailed() =
        runTest {
            val partial =
                BreedSyncResult(
                    succeededSources = setOf(BreedSourceId("a")),
                    failures = mapOf(BreedSourceId("b") to DataError.Remote.NoConnection),
                )
            val allFailed =
                BreedSyncResult(
                    succeededSources = emptySet(),
                    failures =
                        mapOf(
                            BreedSourceId("a") to DataError.Remote.Unauthorized,
                            BreedSourceId("b") to DataError.Remote.NoConnection,
                        ),
                )
            val noSources = BreedSyncResult(succeededSources = emptySet(), failures = emptyMap())

            assertEquals(Outcome.Success(partial), SyncBreedsUseCase(FakeRepository(partial))())
            assertEquals(Outcome.Failure(DataError.Remote.Unauthorized), SyncBreedsUseCase(FakeRepository(allFailed))())
            assertEquals(Outcome.Success(noSources), SyncBreedsUseCase(FakeRepository(noSources))())
        }
}
