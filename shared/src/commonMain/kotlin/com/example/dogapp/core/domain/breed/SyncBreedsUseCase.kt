package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome

class SyncBreedsUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(): Outcome<BreedFetchResult, DataError.Remote> {
        val result = repository.fetchBreeds()
        val everySourceFailed = result.succeededSources.isEmpty() && result.failures.isNotEmpty()
        return if (everySourceFailed) {
            Outcome.Failure(result.failures.values.first())
        } else {
            Outcome.Success(result)
        }
    }
}
