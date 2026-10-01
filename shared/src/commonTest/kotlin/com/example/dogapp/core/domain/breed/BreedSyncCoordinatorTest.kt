package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BreedSyncCoordinatorTest {
    private val source = BreedSourceId("test")
    private val repository = FakeBreedRepository()
    private val success = BreedSyncResult(succeededSources = setOf(source), failures = emptyMap())

    @Test
    fun emptyDatabaseIsSyncedOnceNoMatterHowManyScreensAskForIt() =
        runTest {
            val coordinator = BreedSyncCoordinator(repository, SyncBreedsUseCase(repository), backgroundScope)
            val sync = CompletableDeferred<BreedSyncResult>().also { repository.syncResults += it }

            coordinator.ensureLoaded()
            coordinator.ensureLoaded()
            runCurrent()

            assertEquals(BreedSyncStatus.Syncing, coordinator.status.value)
            repository.breeds.value = listOf(testBreed("akita"))
            sync.complete(success)
            runCurrent()

            assertEquals(BreedSyncStatus.Done, coordinator.status.value)
            coordinator.ensureLoaded()
            runCurrent()
            assertEquals(1, repository.syncCalls)
        }

    @Test
    fun storedBreedsSkipTheFirstSync() =
        runTest {
            repository.breeds.value = listOf(testBreed("akita"))
            val coordinator = BreedSyncCoordinator(repository, SyncBreedsUseCase(repository), backgroundScope)

            coordinator.ensureLoaded()
            runCurrent()

            assertEquals(0, repository.syncCalls)
            assertEquals(BreedSyncStatus.NotStarted, coordinator.status.value)
        }

    @Test
    fun failedSyncIsReportedAndRetrySyncsAgain() =
        runTest {
            val coordinator = BreedSyncCoordinator(repository, SyncBreedsUseCase(repository), backgroundScope)
            repository.syncResults +=
                CompletableDeferred(
                    BreedSyncResult(succeededSources = emptySet(), failures = mapOf(source to DataError.Remote.NoConnection)),
                )

            coordinator.ensureLoaded()
            runCurrent()
            assertEquals(BreedSyncStatus.Failed(DataError.Remote.NoConnection), coordinator.status.value)

            repository.syncResults += CompletableDeferred(success)
            coordinator.retry()
            runCurrent()

            assertEquals(BreedSyncStatus.Done, coordinator.status.value)
            assertEquals(2, repository.syncCalls)
        }
}
