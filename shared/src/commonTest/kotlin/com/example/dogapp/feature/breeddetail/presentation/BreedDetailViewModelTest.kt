package com.example.dogapp.feature.breeddetail.presentation

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.breed.GetBreedGalleryUseCase
import com.example.dogapp.core.domain.breed.ObserveBreedUseCase
import com.example.dogapp.core.domain.favourite.ObserveIsFavouriteUseCase
import com.example.dogapp.core.domain.favourite.ToggleFavouriteUseCase
import com.example.dogapp.testing.FakeFavouritesRepository
import kotlinx.coroutines.CompletableDeferred
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
class BreedDetailViewModelTest {
    private class FakeRepository : BreedRepository {
        val breeds = MutableStateFlow<List<Breed>>(emptyList())
        val gallery = CompletableDeferred<Outcome<List<String>, DataError.Remote>>()
        val galleryRequests = mutableListOf<String>()

        override suspend fun syncBreeds() = BreedSyncResult(succeededSources = emptySet(), failures = emptyMap())

        override fun observeBreeds() = breeds

        override suspend fun fetchGallery(breed: Breed): Outcome<List<String>, DataError.Remote> {
            galleryRequests += breed.id
            return gallery.await()
        }
    }

    private val repository = FakeRepository()

    private fun breed(
        id: String,
        images: List<String>,
    ) = Breed(
        id = id,
        name = id.replaceFirstChar(Char::uppercaseChar),
        weightKg = null,
        heightCm = null,
        lifeSpanYears = null,
        temperament = emptyList(),
        breedGroup = null,
        origin = null,
        description = null,
        history = null,
        traits = emptyMap(),
        imageUrls = images,
        sources = listOf(BreedSourceId("test")),
        sourceNames = listOf("Test"),
        sourceReferences = mapOf(BreedSourceId("test") to id),
    )

    private val favourites = FakeFavouritesRepository()

    private fun viewModel(breedId: String) =
        BreedDetailViewModel(
            breedId = breedId,
            observeBreed = ObserveBreedUseCase(repository),
            getBreedGallery = GetBreedGalleryUseCase(repository),
            observeIsFavourite = ObserveIsFavouriteUseCase(favourites),
            toggleFavourite = ToggleFavouriteUseCase(favourites),
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun breedLoadsWithStoredImagesThenGalleryImagesAreAppendedWithoutDuplicates() =
        runTest {
            val akita = breed("akita", images = listOf("https://stored.jpg"))
            repository.breeds.value = listOf(akita, breed("boxer", images = emptyList()))
            val viewModel = viewModel("akita")
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(
                BreedDetailUiState.Loaded(akita, images = listOf("https://stored.jpg"), isFavourite = false),
                viewModel.state.value,
            )
            assertEquals(listOf("akita"), repository.galleryRequests)

            repository.gallery.complete(Outcome.Success(listOf("https://stored.jpg", "https://gallery/1.jpg")))

            assertEquals(listOf("https://stored.jpg", "https://gallery/1.jpg"), (viewModel.state.value as BreedDetailUiState.Loaded).images)
        }

    @Test
    fun favouriteTogglePersistsThroughTheRepositoryAndGalleryFailureKeepsStoredImages() =
        runTest {
            repository.breeds.value = listOf(breed("akita", images = listOf("https://stored.jpg")))
            val viewModel = viewModel("akita")
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
            repository.gallery.complete(Outcome.Failure(DataError.Remote.NoConnection))

            viewModel.onAction(BreedDetailAction.ToggleFavourite)
            val favourite = viewModel.state.value as BreedDetailUiState.Loaded
            assertEquals(listOf("akita" to "Akita"), favourites.records.value.map { it.breedId to it.name })
            viewModel.onAction(BreedDetailAction.ToggleFavourite)
            val notFavourite = viewModel.state.value as BreedDetailUiState.Loaded

            assertEquals(true, favourite.isFavourite)
            assertEquals(false, notFavourite.isFavourite)
            assertEquals(emptyList(), favourites.records.value)
            assertEquals(listOf("https://stored.jpg"), notFavourite.images)
        }

    @Test
    fun unknownBreedIsReportedAsNotFoundWithoutFetchingAGallery() =
        runTest {
            repository.breeds.value = listOf(breed("akita", images = emptyList()))
            val viewModel = viewModel("poodle")
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(BreedDetailUiState.NotFound, viewModel.state.value)
            assertEquals(emptyList(), repository.galleryRequests)
        }
}
