package com.example.dogapp.core.domain.breed

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveBreedUseCase(
    private val repository: BreedRepository,
) {
    operator fun invoke(breedId: String): Flow<Breed?> =
        repository
            .observeBreeds()
            .map { breeds -> breeds.firstOrNull { it.id == breedId } }
            .distinctUntilChanged()
}
