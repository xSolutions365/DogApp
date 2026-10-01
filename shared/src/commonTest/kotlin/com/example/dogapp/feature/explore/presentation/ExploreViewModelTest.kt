package com.example.dogapp.feature.explore.presentation

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.breed.SearchBreedsUseCase
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import com.example.dogapp.core.domain.favourite.ObserveFavouriteIdsUseCase
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeFavouritesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {
    private val source = BreedSourceId("test")

    private val repository = FakeBreedRepository()

    private fun breed(
        id: String,
        name: String,
        group: String? = null,
        temperament: List<String> = emptyList(),
        images: List<String> = emptyList(),
    ) = Breed(
        id = id,
        name = name,
        weightKg = null,
        heightCm = null,
        lifeSpanYears = null,
        temperament = temperament,
        breedGroup = group,
        origin = null,
        description = null,
        history = null,
        traits = emptyMap(),
        imageUrls = images,
        sources = listOf(source),
        sourceNames = listOf("Test"),
        sourceReferences = emptyMap(),
    )

    private val favourites = FakeFavouritesRepository()

    private val syncScope = CoroutineScope(UnconfinedTestDispatcher())

    private fun viewModel() =
        ExploreViewModel(
            observeBreeds = ObserveBreedsUseCase(repository),
            searchBreeds = SearchBreedsUseCase(),
            breedSync = BreedSyncCoordinator(repository, SyncBreedsUseCase(repository), syncScope),
            observeFavouriteIds = ObserveFavouriteIdsUseCase(favourites),
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        syncScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun emptyDatabaseShowsLoadingWithSearchDisabledAndSyncsOnceUntilBreedsArrive() =
        runTest {
            val sync = CompletableDeferred<BreedSyncResult>().also { repository.syncResults += it }
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(ExploreContent.Loading, viewModel.state.value.content)
            assertFalse(viewModel.state.value.isSearchEnabled)
            assertEquals(1, repository.syncCalls)

            repository.breeds.value =
                listOf(breed("beagle", "Beagle", group = "Hound", images = listOf("https://a/1.jpg", "https://b/1.jpg")))
            sync.complete(BreedSyncResult(succeededSources = setOf(source), failures = emptyMap()))

            assertEquals(
                ExploreContent.Results(
                    listOf(
                        BreedListItem(
                            "beagle",
                            "Beagle",
                            subtitle = "Hound",
                            imageUrls = listOf("https://a/1.jpg", "https://b/1.jpg"),
                            isFavourite = false,
                        ),
                    ),
                ),
                viewModel.state.value.content,
            )
            assertTrue(viewModel.state.value.isSearchEnabled)
            assertEquals(1, repository.syncCalls)
        }

    @Test
    fun failedFirstSyncShowsTheErrorAndRetrySyncsAgain() =
        runTest {
            repository.syncResults +=
                CompletableDeferred(
                    BreedSyncResult(succeededSources = emptySet(), failures = mapOf(source to DataError.Remote.NoConnection)),
                )
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(ExploreContent.Error(DataError.Remote.NoConnection.toStringResource()), viewModel.state.value.content)
            assertFalse(viewModel.state.value.isSearchEnabled)

            val retry = CompletableDeferred<BreedSyncResult>().also { repository.syncResults += it }
            viewModel.onAction(ExploreAction.Retry)
            assertEquals(ExploreContent.Loading, viewModel.state.value.content)
            assertEquals(2, repository.syncCalls)

            repository.breeds.value = listOf(breed("akita", "Akita"))
            retry.complete(BreedSyncResult(succeededSources = setOf(source), failures = emptyMap()))
            assertEquals(listOf("Akita"), (viewModel.state.value.content as ExploreContent.Results).breeds.map { it.name })
        }

    @Test
    fun storedBreedsSkipTheSyncAndQueriesFilterThemOrReportNoMatches() =
        runTest {
            repository.breeds.value =
                listOf(
                    breed("border collie", "Border Collie", temperament = listOf("Intelligent", "energetic", "alert")),
                    breed("rough collie", "Rough Collie", group = "Herding"),
                    breed("beagle", "Beagle"),
                )
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(0, repository.syncCalls)
            assertTrue(viewModel.state.value.isSearchEnabled)
            assertEquals(3, (viewModel.state.value.content as ExploreContent.Results).breeds.size)

            viewModel.onAction(ExploreAction.QueryChange("collie"))
            val results = viewModel.state.value.content as ExploreContent.Results
            assertEquals("collie", viewModel.state.value.query)
            assertEquals(
                listOf(
                    BreedListItem(
                        "border collie",
                        "Border Collie",
                        subtitle = "Intelligent, energetic",
                        imageUrls = emptyList(),
                        isFavourite = false,
                    ),
                    BreedListItem("rough collie", "Rough Collie", subtitle = "Herding", imageUrls = emptyList(), isFavourite = false),
                ),
                results.breeds,
            )

            viewModel.onAction(ExploreAction.QueryChange("poodle"))
            assertEquals(ExploreContent.NoMatches("poodle"), viewModel.state.value.content)
            assertTrue(viewModel.state.value.isSearchEnabled)
        }

    @Test
    fun favouriteBreedsAreMarkedAndTheMarkFollowsFavouriteChanges() =
        runTest {
            repository.breeds.value = listOf(breed("akita", "Akita"), breed("beagle", "Beagle"))
            favourites.toggle("beagle", "Beagle")
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            fun marks() = (viewModel.state.value.content as ExploreContent.Results).breeds.associate { it.id to it.isFavourite }

            assertEquals(mapOf("akita" to false, "beagle" to true), marks())

            favourites.toggle("akita", "Akita")
            favourites.toggle("beagle", "Beagle")

            assertEquals(mapOf("akita" to true, "beagle" to false), marks())
        }
}
