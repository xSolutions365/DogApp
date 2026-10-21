package com.example.dogapp.testing

import com.example.dogapp.core.domain.breed.LastBreedSyncStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant

class FakeLastBreedSyncStore(
    initial: Instant? = null,
) : LastBreedSyncStore {
    val lastSync = MutableStateFlow(initial)

    override fun observeLastSync() = lastSync

    override suspend fun recordSync(at: Instant) {
        lastSync.value = at
    }
}
