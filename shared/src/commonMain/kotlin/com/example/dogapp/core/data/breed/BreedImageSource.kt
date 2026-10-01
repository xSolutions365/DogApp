package com.example.dogapp.core.data.breed

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId

interface BreedImageSource {
    val id: BreedSourceId

    suspend fun fetchImages(sourceReference: String): Outcome<List<String>, DataError.Remote>
}
