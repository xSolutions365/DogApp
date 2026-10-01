package com.example.dogapp.core.domain.breed

import kotlinx.coroutines.flow.Flow

class ObserveBreedsUseCase(
    private val repository: BreedRepository,
) {
    operator fun invoke(): Flow<List<Breed>> = repository.observeBreeds()
}
