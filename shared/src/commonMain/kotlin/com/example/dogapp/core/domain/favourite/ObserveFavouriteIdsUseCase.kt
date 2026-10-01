package com.example.dogapp.core.domain.favourite

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveFavouriteIdsUseCase(
    private val favourites: FavouritesRepository,
) {
    operator fun invoke(): Flow<Set<String>> =
        favourites.observeFavourites().map { records ->
            records.mapTo(mutableSetOf()) { it.breedId }
        }
}
