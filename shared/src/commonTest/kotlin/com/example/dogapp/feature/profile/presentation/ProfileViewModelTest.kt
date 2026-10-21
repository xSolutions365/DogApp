package com.example.dogapp.feature.profile.presentation

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSourceFailure
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import com.example.dogapp.core.domain.draw.DailyDraw
import com.example.dogapp.core.domain.draw.DrawDay
import com.example.dogapp.core.domain.draw.ObserveDrawHistoryUseCase
import com.example.dogapp.core.domain.favourite.FavouriteRecord
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.RemoveFavouriteUseCase
import com.example.dogapp.core.domain.security.IntegrityCheck
import com.example.dogapp.core.domain.security.IntegrityCheckKind
import com.example.dogapp.core.domain.security.IntegrityReport
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeDailyDrawRepository
import com.example.dogapp.testing.FakeDeviceIntegrityChecker
import com.example.dogapp.testing.FakeFavouritesRepository
import com.example.dogapp.testing.FakeLastBreedSyncStore
import com.example.dogapp.testing.FakeTodayProvider
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val breedRepository =
        object : BreedRepository {
            val breeds = MutableStateFlow(listOf(testBreed("akita", breedGroup = "Working", imageUrls = listOf("https://akita.jpg"))))

            override suspend fun syncBreeds() = BreedSyncResult(succeededSources = emptySet(), failures = emptyList())

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

    private val draws = FakeDailyDrawRepository()

    private val today = FakeTodayProvider("2026-10-01")

    private val integrityChecker =
        FakeDeviceIntegrityChecker(
            IntegrityReport(
                listOf(
                    IntegrityCheck(IntegrityCheckKind.Emulator, isFlagged = true),
                    IntegrityCheck(IntegrityCheckKind.Debugger, isFlagged = true),
                    IntegrityCheck(IntegrityCheckKind.AppSignature, isFlagged = false),
                ),
            ),
        )

    private val syncRepository = FakeBreedRepository(breedRepository.breeds.value)

    private val lastSyncStore = FakeLastBreedSyncStore()

    private val syncedAt = Instant.parse("2026-10-21T09:42:00Z")

    private fun viewModel(
        favourites: FakeFavouritesRepository = FakeFavouritesRepository(),
        syncScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
    ) = ProfileViewModel(
        observeFavouriteBreeds = ObserveFavouriteBreedsUseCase(favourites, breedRepository),
        observeDrawHistory = ObserveDrawHistoryUseCase(draws, breedRepository),
        today = today,
        removeFavourite = RemoveFavouriteUseCase(favourites),
        integrityChecker = integrityChecker,
        breedSync =
            BreedSyncCoordinator(
                repository = syncRepository,
                syncBreeds = SyncBreedsUseCase(syncRepository),
                lastSyncStore = lastSyncStore,
                scope = syncScope,
                clock =
                    object : Clock {
                        override fun now() = syncedAt
                    },
            ),
        timeZone = { TimeZone.UTC },
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
                    FavouriteListItem.Available("akita", "Akita", subtitle = "Working", imageUrls = listOf("https://akita.jpg")),
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
    fun noFavouritesIsAnEmptyListOnceLoadedAlongsideTheDeviceCheckWarningCount() =
        runTest {
            val viewModel = viewModel(FakeFavouritesRepository())
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(emptyList(), viewModel.state.value.favourites)
            assertEquals(emptyList(), viewModel.state.value.draws)
            assertEquals(2, viewModel.state.value.deviceStateWarnings)
        }

    @Test
    fun drawHistoryListsOnlyPastDaysNewestFirstAndTodaysDrawJoinsOnceTheDayIsOver() =
        runTest {
            draws.draws.value =
                listOf(
                    DailyDraw(DrawDay("2026-09-28"), "blenheim spaniel", "Blenheim Spaniel", drawnAtEpochMillis = 1),
                    DailyDraw(DrawDay("2026-09-30"), "akita", "Akita", drawnAtEpochMillis = 2),
                    DailyDraw(DrawDay("2026-10-01"), "akita", "Akita", drawnAtEpochMillis = 3),
                ).associateBy { it.day }
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(
                listOf(
                    DrawHistoryItem(
                        day = "2026-09-30",
                        date = "30 Sep 2026",
                        breedId = "akita",
                        name = "Akita",
                        imageUrls = listOf("https://akita.jpg"),
                        isAvailable = true,
                    ),
                    DrawHistoryItem(
                        day = "2026-09-28",
                        date = "28 Sep 2026",
                        breedId = "blenheim spaniel",
                        name = "Blenheim Spaniel",
                        imageUrls = emptyList(),
                        isAvailable = false,
                    ),
                ),
                viewModel.state.value.draws,
            )

            today.day.value = DrawDay("2026-10-02")

            assertEquals(
                listOf("2026-10-01" to "1 Oct 2026", "2026-09-30" to "30 Sep 2026", "2026-09-28" to "28 Sep 2026"),
                viewModel.state.value.draws
                    ?.map { it.day to it.date },
            )
        }

    @Test
    fun syncDataShowsTheLastSyncTimeAndWhileSyncingOrAfterAPartialOrFailedSyncSaysSo() =
        runTest {
            val syncScope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
            val viewModel = viewModel(syncScope = syncScope)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(SyncDataStatus.Idle(lastSynced = null), viewModel.state.value.syncData)
            lastSyncStore.lastSync.value = Instant.parse("2026-10-20T18:05:00Z")
            assertEquals(SyncDataStatus.Idle(lastSynced = "20 Oct 2026, 18:05"), viewModel.state.value.syncData)

            val partial = CompletableDeferred<BreedSyncResult>().also { syncRepository.syncResults += it }
            viewModel.onAction(ProfileAction.SyncData)
            assertEquals(SyncDataStatus.Syncing, viewModel.state.value.syncData)
            partial.complete(
                BreedSyncResult(
                    succeededSources = setOf(BreedSourceId("the-dog-api")),
                    failures = listOf(BreedSourceFailure(BreedSourceId("api-ninjas"), "API Ninjas", DataError.Remote.NoConnection)),
                ),
            )
            assertEquals(SyncDataStatus.PartlySynced(listOf("API Ninjas")), viewModel.state.value.syncData)

            syncRepository.syncResults +=
                CompletableDeferred(
                    BreedSyncResult(
                        succeededSources = emptySet(),
                        failures = listOf(BreedSourceFailure(BreedSourceId("the-dog-api"), "TheDogAPI", DataError.Remote.NoConnection)),
                    ),
                )
            viewModel.onAction(ProfileAction.SyncData)
            assertEquals(SyncDataStatus.Failed, viewModel.state.value.syncData)

            syncRepository.syncResults +=
                CompletableDeferred(BreedSyncResult(succeededSources = setOf(BreedSourceId("the-dog-api")), failures = emptyList()))
            viewModel.onAction(ProfileAction.SyncData)
            assertEquals(SyncDataStatus.Idle(lastSynced = "21 Oct 2026, 09:42"), viewModel.state.value.syncData)
            assertEquals(3, syncRepository.syncCalls)
        }
}
