package com.example.dogapp.core.domain.favourite

class ToggleFavouriteUseCase(
    private val favourites: FavouritesRepository,
) {
    suspend operator fun invoke(
        breedId: String,
        name: String,
    ) = favourites.toggle(breedId, name)
}
