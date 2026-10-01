package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import kotlinx.coroutines.flow.Flow

interface BreedRepository {
    suspend fun syncBreeds(): BreedSyncResult

    fun observeBreeds(): Flow<List<Breed>>

    suspend fun fetchGallery(breed: Breed): Outcome<List<String>, DataError.Remote>
}
