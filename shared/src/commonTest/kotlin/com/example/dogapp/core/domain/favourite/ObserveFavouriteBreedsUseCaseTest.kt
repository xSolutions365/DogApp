package com.example.dogapp.core.domain.favourite

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.testing.FakeFavouritesRepository
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveFavouriteBreedsUseCaseTest {
    private class FakeBreedRepository(
        breeds: List<Breed>,
    ) : BreedRepository {
        val breeds = MutableStateFlow(breeds)

        override suspend fun syncBreeds() = BreedSyncResult(succeededSources = emptySet(), failures = emptyMap())

        override fun observeBreeds() = breeds

        override suspend fun fetchGallery(breed: Breed): Outcome<List<String>, DataError.Remote> = Outcome.Success(emptyList())
    }

    @Test
    fun favouritesResolveToCurrentBreedsNewestFirstAndVanishedBreedsKeepTheirSavedName() =
        runTest {
            val akita = testBreed("akita", breedGroup = "Working")
            val breeds = FakeBreedRepository(listOf(akita))
            val favourites =
                FakeFavouritesRepository(
                    listOf(
                        FavouriteRecord("akita", "Akita", addedAtEpochMillis = 1),
                        FavouriteRecord("blenheim spaniel", "Blenheim Spaniel", addedAtEpochMillis = 2),
                    ),
                )
            val observe = ObserveFavouriteBreedsUseCase(favourites, breeds)

            assertEquals(
                listOf(FavouriteBreed.Unavailable("blenheim spaniel", "Blenheim Spaniel"), FavouriteBreed.Available(akita)),
                observe().first(),
            )

            val blenheim = testBreed("blenheim spaniel")
            breeds.breeds.value = listOf(akita, blenheim)
            assertEquals(listOf(FavouriteBreed.Available(blenheim), FavouriteBreed.Available(akita)), observe().first())
        }
}
