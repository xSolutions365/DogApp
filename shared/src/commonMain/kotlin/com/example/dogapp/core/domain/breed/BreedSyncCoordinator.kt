package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Instant

class BreedSyncCoordinator(
    private val repository: BreedRepository,
    private val syncBreeds: SyncBreedsUseCase,
    private val lastSyncStore: LastBreedSyncStore,
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow<BreedSyncStatus>(BreedSyncStatus.NotStarted)

    val status: StateFlow<BreedSyncStatus> = mutableStatus.asStateFlow()

    val lastSyncedAt: Flow<Instant?> = lastSyncStore.observeLastSync()

    fun ensureLoaded() {
        scope.launch {
            mutex.withLock {
                if (status.value !is BreedSyncStatus.Done && repository.observeBreeds().first().isEmpty()) sync()
            }
        }
    }

    fun syncNow() {
        scope.launch { mutex.withLock { sync() } }
    }

    private suspend fun sync() {
        mutableStatus.value = BreedSyncStatus.Syncing
        mutableStatus.value =
            when (val outcome = syncBreeds()) {
                is Outcome.Success -> {
                    lastSyncStore.recordSync(clock.now())
                    BreedSyncStatus.Done(unavailableSources = outcome.data.failures.map { it.sourceName })
                }

                is Outcome.Failure -> {
                    BreedSyncStatus.Failed(outcome.error)
                }
            }
    }
}
