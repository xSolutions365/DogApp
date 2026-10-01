package com.example.dogapp.core.domain.favourite

import com.example.dogapp.core.domain.breed.Breed

data class FavouriteRecord(
    val breedId: String,
    val name: String,
    val addedAtEpochMillis: Long,
)

sealed interface FavouriteBreed {
    val breedId: String

    data class Available(
        val breed: Breed,
    ) : FavouriteBreed {
        override val breedId: String get() = breed.id
    }

    data class Unavailable(
        override val breedId: String,
        val name: String,
    ) : FavouriteBreed
}
