package com.example.dogapp.core.domain.favourite

import com.example.dogapp.core.domain.breed.BreedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveFavouriteBreedsUseCase(
    private val favourites: FavouritesRepository,
    private val breeds: BreedRepository,
) {
    operator fun invoke(): Flow<List<FavouriteBreed>> =
        combine(favourites.observeFavourites(), breeds.observeBreeds()) { records, breeds ->
            val breedsById = breeds.associateBy { it.id }
            records.map { record ->
                breedsById[record.breedId]?.let { FavouriteBreed.Available(it) }
                    ?: FavouriteBreed.Unavailable(breedId = record.breedId, name = record.name)
            }
        }
}
