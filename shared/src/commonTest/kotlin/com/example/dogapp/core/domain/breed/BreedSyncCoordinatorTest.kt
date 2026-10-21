package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeLastBreedSyncStore
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class BreedSyncCoordinatorTest {
    private val source = BreedSourceId("test")
    private val repository = FakeBreedRepository()
    private val lastSyncStore = FakeLastBreedSyncStore()
    private val success = BreedSyncResult(succeededSources = setOf(source), failures = emptyList())
    private val syncedAt = Instant.fromEpochMilliseconds(1_790_000_000_000)

    private fun coordinator(scope: CoroutineScope) =
        BreedSyncCoordinator(
            repository = repository,
            syncBreeds = SyncBreedsUseCase(repository),
            lastSyncStore = lastSyncStore,
            scope = scope,
            clock =
                object : Clock {
                    override fun now() = syncedAt
                },
        )

    @Test
    fun emptyDatabaseIsSyncedOnceNoMatterHowManyScreensAskForItAndTheSyncTimeIsKept() =
        runTest {
            val coordinator = coordinator(backgroundScope)
            val sync = CompletableDeferred<BreedSyncResult>().also { repository.syncResults += it }

            coordinator.ensureLoaded()
            coordinator.ensureLoaded()
            runCurrent()

            assertEquals(BreedSyncStatus.Syncing, coordinator.status.value)
            repository.breeds.value = listOf(testBreed("akita"))
            sync.complete(success)
            runCurrent()

            assertEquals(BreedSyncStatus.Done(unavailableSources = emptyList()), coordinator.status.value)
            assertEquals(syncedAt, lastSyncStore.lastSync.value)
            coordinator.ensureLoaded()
            runCurrent()
            assertEquals(1, repository.syncCalls)
        }

    @Test
    fun storedBreedsSkipTheFirstSync() =
        runTest {
            repository.breeds.value = listOf(testBreed("akita"))
            val coordinator = coordinator(backgroundScope)

            coordinator.ensureLoaded()
            runCurrent()

            assertEquals(0, repository.syncCalls)
            assertEquals(BreedSyncStatus.NotStarted, coordinator.status.value)
        }

    @Test
    fun failedSyncIsReportedWithoutASyncTimeAndSyncNowSyncsAgain() =
        runTest {
            val coordinator = coordinator(backgroundScope)
            repository.syncResults +=
                CompletableDeferred(
                    BreedSyncResult(
                        succeededSources = emptySet(),
                        failures = listOf(BreedSourceFailure(source, "Test", DataError.Remote.NoConnection)),
                    ),
                )

            coordinator.ensureLoaded()
            runCurrent()
            assertEquals(BreedSyncStatus.Failed(DataError.Remote.NoConnection), coordinator.status.value)
            assertEquals(null, lastSyncStore.lastSync.value)

            repository.syncResults += CompletableDeferred(success)
            coordinator.syncNow()
            runCurrent()

            assertEquals(BreedSyncStatus.Done(unavailableSources = emptyList()), coordinator.status.value)
            assertEquals(syncedAt, lastSyncStore.lastSync.value)
            assertEquals(2, repository.syncCalls)
        }

    @Test
    fun syncNowResyncsStoredBreedsAndNamesTheSourcesThatDidNotRespond() =
        runTest {
            repository.breeds.value = listOf(testBreed("akita"))
            val coordinator = coordinator(backgroundScope)
            val sync = CompletableDeferred<BreedSyncResult>().also { repository.syncResults += it }

            coordinator.syncNow()
            runCurrent()
            assertEquals(BreedSyncStatus.Syncing, coordinator.status.value)

            sync.complete(
                BreedSyncResult(
                    succeededSources = setOf(source),
                    failures = listOf(BreedSourceFailure(BreedSourceId("api-ninjas"), "API Ninjas", DataError.Remote.NoConnection)),
                ),
            )
            runCurrent()

            assertEquals(BreedSyncStatus.Done(unavailableSources = listOf("API Ninjas")), coordinator.status.value)
            assertEquals(syncedAt, lastSyncStore.lastSync.value)
            assertEquals(1, repository.syncCalls)
        }
}
