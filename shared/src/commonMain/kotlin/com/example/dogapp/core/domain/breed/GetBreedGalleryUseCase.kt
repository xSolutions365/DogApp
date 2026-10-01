package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome

class GetBreedGalleryUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(breed: Breed): Outcome<List<String>, DataError.Remote> = repository.fetchGallery(breed)
}
