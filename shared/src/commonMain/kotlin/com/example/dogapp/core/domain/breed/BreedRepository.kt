package com.example.dogapp.core.domain.breed

interface BreedRepository {
    suspend fun fetchBreeds(): BreedFetchResult
}
