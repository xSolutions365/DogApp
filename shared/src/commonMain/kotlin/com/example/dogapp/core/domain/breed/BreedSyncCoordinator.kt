package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BreedSyncCoordinator(
    private val repository: BreedRepository,
    private val syncBreeds: SyncBreedsUseCase,
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow<BreedSyncStatus>(BreedSyncStatus.NotStarted)

    val status: StateFlow<BreedSyncStatus> = mutableStatus.asStateFlow()

    fun ensureLoaded() {
        scope.launch {
            mutex.withLock {
                if (status.value != BreedSyncStatus.Done && repository.observeBreeds().first().isEmpty()) sync()
            }
        }
    }

    fun retry() {
        scope.launch { mutex.withLock { sync() } }
    }

    private suspend fun sync() {
        mutableStatus.value = BreedSyncStatus.Syncing
        mutableStatus.value =
            when (val outcome = syncBreeds()) {
                is Outcome.Success -> BreedSyncStatus.Done
                is Outcome.Failure -> BreedSyncStatus.Failed(outcome.error)
            }
    }
}
