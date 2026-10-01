package com.example.dogapp.core.data.favourite

import com.example.dogapp.core.data.favourite.local.FavouriteBreedDao
import com.example.dogapp.core.data.favourite.local.FavouriteBreedEntity
import com.example.dogapp.core.domain.favourite.FavouriteRecord
import com.example.dogapp.core.domain.favourite.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class DefaultFavouritesRepository(
    private val dao: FavouriteBreedDao,
    private val clock: Clock = Clock.System,
) : FavouritesRepository {
    override fun observeFavourites(): Flow<List<FavouriteRecord>> =
        dao.observeAll().map { entities -> entities.map { FavouriteRecord(it.breedId, it.name, it.addedAtEpochMillis) } }

    override fun observeIsFavourite(breedId: String): Flow<Boolean> = dao.observeIsFavourite(breedId)

    override suspend fun toggle(
        breedId: String,
        name: String,
    ) = dao.toggle(FavouriteBreedEntity(breedId = breedId, name = name, addedAtEpochMillis = clock.now().toEpochMilliseconds()))

    override suspend fun remove(breedId: String) = dao.delete(breedId)
}
