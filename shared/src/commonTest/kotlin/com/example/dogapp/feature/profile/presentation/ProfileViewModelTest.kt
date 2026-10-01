package com.example.dogapp.feature.profile.presentation

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.favourite.FavouriteRecord
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.RemoveFavouriteUseCase
import com.example.dogapp.testing.FakeFavouritesRepository
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val breedRepository =
        object : BreedRepository {
            val breeds = MutableStateFlow(listOf(testBreed("akita", breedGroup = "Working", imageUrls = listOf("https://akita.jpg"))))

            override suspend fun syncBreeds() = BreedSyncResult(succeededSources = emptySet(), failures = emptyMap())

            override fun observeBreeds() = breeds

            override suspend fun fetchGallery(breed: Breed): Outcome<List<String>, DataError.Remote> = Outcome.Success(emptyList())
        }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(favourites: FakeFavouritesRepository) =
        ProfileViewModel(
            observeFavouriteBreeds = ObserveFavouriteBreedsUseCase(favourites, breedRepository),
            removeFavourite = RemoveFavouriteUseCase(favourites),
        )

    @Test
    fun favouritesListAvailableBreedsAndVanishedOnesAsUnavailableAndCanBeRemoved() =
        runTest {
            val favourites =
                FakeFavouritesRepository(
                    listOf(
                        FavouriteRecord("akita", "Akita", addedAtEpochMillis = 1),
                        FavouriteRecord("blenheim spaniel", "Blenheim Spaniel", addedAtEpochMillis = 2),
                    ),
                )
            val viewModel = viewModel(favourites)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(
                listOf(
                    FavouriteListItem.Unavailable("blenheim spaniel", "Blenheim Spaniel"),
                    FavouriteListItem.Available("akita", "Akita", subtitle = "Working", imageUrl = "https://akita.jpg"),
                ),
                viewModel.state.value.favourites,
            )

            viewModel.onAction(ProfileAction.RemoveFavourite("blenheim spaniel"))

            assertEquals(
                listOf("akita"),
                viewModel.state.value.favourites
                    ?.map { it.breedId },
            )
        }

    @Test
    fun noFavouritesIsAnEmptyListOnceLoaded() =
        runTest {
            val viewModel = viewModel(FakeFavouritesRepository())
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(emptyList(), viewModel.state.value.favourites)
        }
}
