package com.example.dogapp.feature.explore.presentation

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSourceFailure
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.breed.SearchBreedsUseCase
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import com.example.dogapp.core.domain.favourite.ObserveFavouriteIdsUseCase
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.feature.explore.domain.AvailableFiltersUseCase
import com.example.dogapp.feature.explore.domain.BreedFilters
import com.example.dogapp.feature.explore.domain.BreedInfo
import com.example.dogapp.feature.explore.domain.BreedSize
import com.example.dogapp.feature.explore.domain.FilterBreedsUseCase
import com.example.dogapp.feature.explore.domain.FilterCategory
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeFavouritesRepository
import com.example.dogapp.testing.FakeLastBreedSyncStore
import com.example.dogapp.testing.testBreed
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

    private val breedSync = BreedSyncCoordinator(repository, SyncBreedsUseCase(repository), FakeLastBreedSyncStore(), syncScope)

    private fun viewModel() =
        ExploreViewModel(
            observeBreeds = ObserveBreedsUseCase(repository),
            searchBreeds = SearchBreedsUseCase(),
            filterBreeds = FilterBreedsUseCase(),
            availableFilters = AvailableFiltersUseCase(),
            breedSync = breedSync,
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
            sync.complete(BreedSyncResult(succeededSources = setOf(source), failures = emptyList()))

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
                    BreedSyncResult(
                        succeededSources = emptySet(),
                        failures = listOf(BreedSourceFailure(source, "Test", DataError.Remote.NoConnection)),
                    ),
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
            retry.complete(BreedSyncResult(succeededSources = setOf(source), failures = emptyList()))
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

    @Test
    fun filtersCombineWithTheQueryReportHiddenBreedsOfferOnlyFiltersWithStoredInfoAndSurviveAResync() =
        runTest {
            repository.breeds.value =
                listOf(
                    testBreed("border collie", breedGroup = "Herding", weightKg = MeasureRange(14.0, 20.0)),
                    testBreed("rough collie", breedGroup = "Herding"),
                    testBreed("beagle", breedGroup = "Hound", weightKg = MeasureRange(9.0, 13.0)),
                    testBreed("pug", weightKg = MeasureRange(6.0, 8.0)),
                )
            favourites.toggle("pug", "Pug")
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            fun shown() = (viewModel.state.value.content as ExploreContent.Results).breeds.map { it.id }

            fun hidden() = (viewModel.state.value.content as ExploreContent.Results).hiddenForMissingInfo

            assertEquals(setOf(FilterCategory.Size, FilterCategory.Group), viewModel.state.value.availableFilters)
            assertEquals(listOf("Herding", "Hound"), viewModel.state.value.groupOptions)

            val medium = BreedFilters(sizes = setOf(BreedSize.Medium))
            viewModel.onAction(ExploreAction.FiltersChange(medium))
            assertEquals(medium, viewModel.state.value.filters)
            assertEquals(listOf("border collie", "beagle"), shown())
            assertEquals(mapOf(BreedInfo.Size to 1), hidden())

            viewModel.onAction(ExploreAction.QueryChange("collie"))
            assertEquals(listOf("border collie"), shown())
            assertEquals(mapOf(BreedInfo.Size to 1), hidden())

            viewModel.onAction(ExploreAction.FiltersChange(medium.copy(favouritesOnly = true)))
            assertEquals(ExploreContent.NoMatches("collie", hasFilters = true), viewModel.state.value.content)

            viewModel.onAction(ExploreAction.QueryChange(""))
            assertEquals(ExploreContent.NoMatches("", hasFilters = true), viewModel.state.value.content)

            viewModel.onAction(ExploreAction.FiltersChange(BreedFilters(favouritesOnly = true)))
            assertEquals(listOf("pug"), shown())
            assertEquals(emptyMap(), hidden())

            val favouritesOnly = BreedFilters(favouritesOnly = true)
            val resync = CompletableDeferred<BreedSyncResult>().also { repository.syncResults += it }
            breedSync.syncNow()
            assertEquals(listOf("pug"), shown())
            assertEquals(favouritesOnly, viewModel.state.value.filters)
            assertTrue(viewModel.state.value.isSearchEnabled)

            repository.breeds.value += testBreed("akita")
            resync.complete(BreedSyncResult(succeededSources = setOf(source), failures = emptyList()))
            assertEquals(listOf("pug"), shown())
            viewModel.onAction(ExploreAction.FiltersChange(BreedFilters()))
            assertEquals(listOf("border collie", "rough collie", "beagle", "pug", "akita"), shown())
        }
}
