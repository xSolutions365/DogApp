package com.example.dogapp.feature.home.presentation

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import com.example.dogapp.core.domain.draw.DailyDraw
import com.example.dogapp.core.domain.draw.DrawBreedOfTheDayUseCase
import com.example.dogapp.core.domain.draw.DrawDay
import com.example.dogapp.core.domain.draw.ObserveTodaysDrawUseCase
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeDailyDrawRepository
import com.example.dogapp.testing.FakeTodayProvider
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val source = BreedSourceId("test")
    private val breeds = FakeBreedRepository()
    private val draws = FakeDailyDrawRepository()
    private val today = FakeTodayProvider("2026-10-01")
    private val syncScope = CoroutineScope(UnconfinedTestDispatcher())
    private val akita = testBreed("akita", breedGroup = "Working", imageUrls = listOf("https://akita.jpg"))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        syncScope.cancel()
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        HomeViewModel(
            observeBreeds = ObserveBreedsUseCase(breeds),
            observeTodaysDraw = ObserveTodaysDrawUseCase(draws, breeds, today),
            drawBreedOfTheDay = DrawBreedOfTheDayUseCase(draws, breeds, today),
            breedSync = BreedSyncCoordinator(breeds, SyncBreedsUseCase(breeds), syncScope),
        )

    @Test
    fun emptyDatabaseShowsLoadingUntilTheFirstSyncBringsBreedsToDraw() =
        runTest {
            val sync = CompletableDeferred<BreedSyncResult>().also { breeds.syncResults += it }
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(HomeContent.Loading, viewModel.state.value.content)

            breeds.breeds.value = listOf(akita)
            sync.complete(BreedSyncResult(succeededSources = setOf(source), failures = emptyMap()))

            assertEquals(HomeContent.NotRevealed(isDrawing = false), viewModel.state.value.content)
        }

    @Test
    fun failedFirstSyncShowsTheErrorAndRetrySyncsAgain() =
        runTest {
            breeds.syncResults +=
                CompletableDeferred(
                    BreedSyncResult(succeededSources = emptySet(), failures = mapOf(source to DataError.Remote.NoConnection)),
                )
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(HomeContent.Error(DataError.Remote.NoConnection.toStringResource()), viewModel.state.value.content)

            breeds.syncResults += CompletableDeferred(BreedSyncResult(succeededSources = setOf(source), failures = emptyMap()))
            viewModel.onAction(HomeAction.Retry)

            assertEquals(HomeContent.NoBreeds, viewModel.state.value.content)
            assertEquals(2, breeds.syncCalls)
        }

    @Test
    fun revealingDrawsTodaysBreedOnceAndANewDayOffersANewDraw() =
        runTest {
            breeds.breeds.value = listOf(akita)
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            viewModel.onAction(HomeAction.Reveal)
            viewModel.onAction(HomeAction.Reveal)

            assertEquals(
                HomeContent.Revealed(DrawnBreed.Available("akita", "Akita", subtitle = "Working", imageUrls = listOf("https://akita.jpg"))),
                viewModel.state.value.content,
            )
            assertEquals(
                listOf(DrawDay("2026-10-01")),
                draws.draws.value.keys
                    .toList(),
            )

            today.day.value = DrawDay("2026-10-02")

            assertEquals(HomeContent.NotRevealed(isDrawing = false), viewModel.state.value.content)
        }

    @Test
    fun todaysDrawOfABreedNoLongerInTheCatalogueIsShownByItsSavedName() =
        runTest {
            breeds.breeds.value = listOf(akita)
            draws.draws.value = mapOf(DrawDay("2026-10-01") to DailyDraw(DrawDay("2026-10-01"), "blenheim spaniel", "Blenheim Spaniel", 1))
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(
                HomeContent.Revealed(DrawnBreed.Unavailable("blenheim spaniel", "Blenheim Spaniel")),
                viewModel.state.value.content,
            )
        }
}
