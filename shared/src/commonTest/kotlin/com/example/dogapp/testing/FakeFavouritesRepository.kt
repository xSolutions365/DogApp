package com.example.dogapp.testing

import com.example.dogapp.core.domain.favourite.FavouriteRecord
import com.example.dogapp.core.domain.favourite.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavouritesRepository(
    initial: List<FavouriteRecord> = emptyList(),
) : FavouritesRepository {
    val records = MutableStateFlow(initial)
    private var clock = initial.maxOfOrNull { it.addedAtEpochMillis } ?: 0L

    override fun observeFavourites(): Flow<List<FavouriteRecord>> =
        records.map { list -> list.sortedByDescending { it.addedAtEpochMillis } }

    override fun observeIsFavourite(breedId: String): Flow<Boolean> = records.map { list -> list.any { it.breedId == breedId } }

    override suspend fun toggle(
        breedId: String,
        name: String,
    ) {
        records.value =
            if (records.value.any { it.breedId == breedId }) {
                records.value.filterNot { it.breedId == breedId }
            } else {
                records.value + FavouriteRecord(breedId, name, ++clock)
            }
    }

    override suspend fun remove(breedId: String) {
        records.value = records.value.filterNot { it.breedId == breedId }
    }
}
