package com.example.dogapp.core.domain.favourite

import kotlinx.coroutines.flow.Flow

interface FavouritesRepository {
    fun observeFavourites(): Flow<List<FavouriteRecord>>

    fun observeIsFavourite(breedId: String): Flow<Boolean>

    suspend fun toggle(
        breedId: String,
        name: String,
    )

    suspend fun remove(breedId: String)
}
