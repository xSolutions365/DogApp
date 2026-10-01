package com.example.dogapp.core.domain.breed

import kotlinx.coroutines.flow.Flow

interface BreedRepository {
    suspend fun syncBreeds(): BreedSyncResult

    fun observeBreeds(): Flow<List<Breed>>
}
