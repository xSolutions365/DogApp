package com.example.dogapp.core.domain.favourite

import kotlinx.coroutines.flow.Flow

class ObserveIsFavouriteUseCase(
    private val favourites: FavouritesRepository,
) {
    operator fun invoke(breedId: String): Flow<Boolean> = favourites.observeIsFavourite(breedId)
}
