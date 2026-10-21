package com.example.dogapp.core.domain.breed

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface LastBreedSyncStore {
    fun observeLastSync(): Flow<Instant?>

    suspend fun recordSync(at: Instant)
}
