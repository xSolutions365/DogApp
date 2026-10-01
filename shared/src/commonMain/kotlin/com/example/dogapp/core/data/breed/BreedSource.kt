package com.example.dogapp.core.data.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.SourcedBreed

interface BreedSource {
    val id: BreedSourceId
    val precedence: Int

    suspend fun fetchBreeds(): Outcome<List<SourcedBreed>, DataError.Remote>
}
