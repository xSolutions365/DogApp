package com.example.dogapp.testing

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBreedRepository(
    initial: List<Breed> = emptyList(),
) : BreedRepository {
    val breeds = MutableStateFlow(initial)
    val syncResults = ArrayDeque<CompletableDeferred<BreedSyncResult>>()
    var syncCalls = 0
        private set

    override suspend fun syncBreeds(): BreedSyncResult {
        syncCalls++
        return syncResults.removeFirst().await()
    }

    override fun observeBreeds() = breeds

    override suspend fun fetchGallery(breed: Breed) = Outcome.Success(emptyList<String>())
}
