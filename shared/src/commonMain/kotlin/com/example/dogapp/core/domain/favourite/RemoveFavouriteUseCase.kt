package com.example.dogapp.core.domain.favourite

class RemoveFavouriteUseCase(
    private val favourites: FavouritesRepository,
) {
    suspend operator fun invoke(breedId: String) = favourites.remove(breedId)
}
