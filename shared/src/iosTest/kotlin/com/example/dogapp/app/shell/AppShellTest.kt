package com.example.dogapp.app.shell

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.example.dogapp.app.di.applicationScopeModule
import com.example.dogapp.app.di.breedModule
import com.example.dogapp.app.di.drawModule
import com.example.dogapp.app.di.favouriteModule
import com.example.dogapp.app.di.paymentModule
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.core.data.security.IosDeviceIntegrityChecker
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.breed.LastBreedSyncStore
import com.example.dogapp.core.domain.draw.DailyDrawRepository
import com.example.dogapp.core.domain.draw.DrawDay
import com.example.dogapp.core.domain.draw.TodayProvider
import com.example.dogapp.core.domain.favourite.FavouriteRecord
import com.example.dogapp.core.domain.favourite.FavouritesRepository
import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.breeddetail.di.breedDetailModule
import com.example.dogapp.feature.donate.di.donateModule
import com.example.dogapp.feature.donate.domain.DonationRepository
import com.example.dogapp.feature.explore.di.exploreModule
import com.example.dogapp.feature.home.di.homeModule
import com.example.dogapp.feature.identify.data.ImageLabeler
import com.example.dogapp.feature.identify.di.identifyModule
import com.example.dogapp.feature.profile.di.profileModule
import com.example.dogapp.feature.wallet.data.WalletVault
import com.example.dogapp.feature.wallet.di.walletModule
import com.example.dogapp.testing.FakeDailyDrawRepository
import com.example.dogapp.testing.FakeDonationRepository
import com.example.dogapp.testing.FakeFavouritesRepository
import com.example.dogapp.testing.FakeLastBreedSyncStore
import com.example.dogapp.testing.FakeTodayProvider
import com.example.dogapp.testing.FakeWalletVault
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import platform.UIKit.UIViewController
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppShellTest {
    private val isTab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) = isTab and hasText(label)

    private fun screenTitle(label: String) = isHeading() and hasText(label)

    private val homeScreen = screenTitle("Your breed of the day")

    private val verticalList = hasScrollToNodeAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

    private fun ComposeUiTest.assertDetailTitle(name: String) {
        onNode(verticalList).performScrollToNode(screenTitle(name))
        onNode(screenTitle(name)).assertExists()
    }

    private val exploreSearch = SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)

    private val borderCollie =
        Breed(
            id = "border collie",
            name = "Border Collie",
            weightKg = null,
            heightCm = null,
            lifeSpanYears = null,
            temperament = emptyList(),
            breedGroup = "Herding",
            origin = null,
            description = null,
            history = null,
            traits = emptyMap(),
            imageUrls = emptyList(),
            sources = emptyList(),
            sourceNames = emptyList(),
            sourceReferences = emptyMap(),
        )

    private val draws = FakeDailyDrawRepository()

    private val today = FakeTodayProvider("2026-10-01")

    private val favourites = FakeFavouritesRepository(listOf(FavouriteRecord("border collie", "Border Collie", addedAtEpochMillis = 1)))

    private val emptyBreedRepository =
        object : BreedRepository {
            override suspend fun syncBreeds() = BreedSyncResult(succeededSources = emptySet(), failures = emptyList())

            override fun observeBreeds() = MutableStateFlow(listOf(borderCollie))

            override suspend fun fetchGallery(breed: Breed) = Outcome.Success(emptyList<String>())
        }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    private fun ComposeUiTest.setShell(style: NavigationStyle): DirectNavigationEventInput {
        stopKoin()
        val backInput = DirectNavigationEventInput()
        setContent {
            val dispatcher = requireNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
            remember(dispatcher) { dispatcher.addInput(backInput) }
            KoinApplication(
                configuration =
                    koinConfiguration {
                        modules(
                            applicationScopeModule,
                            breedModule(theDogApiKey = null, apiNinjasKey = null),
                            favouriteModule,
                            drawModule,
                            homeModule,
                            exploreModule,
                            breedDetailModule,
                            profileModule,
                            identifyModule,
                            donateModule,
                            walletModule,
                            paymentModule,
                            module {
                                single<BreedRepository> { emptyBreedRepository }
                                single<LastBreedSyncStore> { FakeLastBreedSyncStore() }
                                single<FavouritesRepository> { favourites }
                                single<DailyDrawRepository> { draws }
                                single<TodayProvider> { today }
                                single<WalletVault> { FakeWalletVault() }
                                single<DonationRepository> { FakeDonationRepository() }
                                single<DeviceIntegrityChecker> { IosDeviceIntegrityChecker() }
                                single<ImageLabeler> {
                                    object : ImageLabeler {
                                        override suspend fun label(photo: ByteArray) = Outcome.Success(emptyMap<String, Float>())
                                    }
                                }
                            },
                        )
                    },
            ) {
                CompositionLocalProvider(LocalUIViewController provides UIViewController()) {
                    DogAppTheme {
                        AppShell(
                            navigationStyle = style,
                            identifyMenuButton = { _, onChoosePhotoClick -> TextButton(onClick = onChoosePhotoClick) { Text("Identify") } },
                        )
                    }
                }
            }
        }
        return backInput
    }

    private fun ComposeUiTest.performPredictiveBack(backInput: DirectNavigationEventInput) {
        runOnIdle { backInput.backStarted(NavigationEvent(progress = 0f, swipeEdge = NavigationEvent.EDGE_LEFT)) }
        runOnIdle { backInput.backProgressed(NavigationEvent(progress = 0.5f, swipeEdge = NavigationEvent.EDGE_LEFT)) }
        runOnIdle { backInput.backCompleted() }
        waitForIdle()
    }

    @Test
    fun tabsSwitchScreensAndReflectSelectionInBothNavigationStyles() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                setShell(style)

                onNode(tab("Home")).assertIsSelected()
                onNode(homeScreen).assertExists()

                onNode(tab("Explore")).performClick()
                onNode(tab("Explore")).assertIsSelected()
                onNode(tab("Home")).assertIsNotSelected()
                onNode(exploreSearch).assertExists()
                onNode(homeScreen).assertDoesNotExist()

                onNode(tab("Profile")).performClick()
                onNode(tab("Profile")).assertIsSelected()
                onNode(screenTitle("Profile")).assertExists()

                onNode(tab("Home")).performClick()
                onNode(tab("Home")).assertIsSelected()
                onNode(homeScreen).assertExists()
                onNode(screenTitle("Profile")).assertDoesNotExist()
            }
        }
    }

    @Test
    fun predictiveBackFromNonStartTabRootFollowsPlatformConvention() {
        runComposeUiTest {
            val backInput = setShell(NavigationStyle.Material)
            onNode(tab("Explore")).performClick()

            performPredictiveBack(backInput)

            onNode(tab("Home")).assertIsSelected()
            onNode(homeScreen).assertExists()
            onNode(exploreSearch).assertDoesNotExist()
        }
        runComposeUiTest {
            val backInput = setShell(NavigationStyle.Cupertino)
            onNode(tab("Explore")).performClick()

            performPredictiveBack(backInput)

            onNode(tab("Explore")).assertIsSelected()
            onNode(exploreSearch).assertExists()
        }
    }

    @Test
    fun tappingABreedInExploreOpensItsDetailOnTheExploreTabAndBackReturns() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                val backInput = setShell(style)
                onNode(tab("Explore")).performClick()

                onNode(hasText("Border Collie")).performClick()
                waitForIdle()

                assertDetailTitle("Border Collie")
                onNode(tab("Explore")).assertIsSelected()

                performPredictiveBack(backInput)

                onNode(screenTitle("Border Collie")).assertDoesNotExist()
                onNode(exploreSearch).assertExists()
            }
        }
    }

    @Test
    fun revealingTheBreedOfTheDayOnHomeOpensItsDetailAndListsItInProfileOnlyOnceTheDayIsOver() {
        runComposeUiTest {
            setShell(NavigationStyle.Material)

            onNode(hasClickAction() and hasText("Reveal today's breed")).performClick()
            waitForIdle()
            onNode(hasText("Today's breed") and isHeading()).assertExists()
            onNode(hasClickAction() and hasText("Border Collie", substring = true)).performClick()
            waitForIdle()

            assertDetailTitle("Border Collie")
            onNode(tab("Home")).assertIsSelected()

            onNode(tab("Profile")).performClick()
            onNode(verticalList).performScrollToNode(hasText("Each breed of the day you reveal will appear here the following day."))
            onNode(hasText("1 Oct 2026")).assertDoesNotExist()

            today.day.value = DrawDay("2026-10-02")
            waitForIdle()

            onNode(verticalList).performScrollToNode(hasText("1 Oct 2026"))
            onNode(hasText("Border Collie") and hasText("1 Oct 2026")).assertExists()
        }
    }

    @Test
    fun theIdentifyMenuOnMaterialHomeOpensIdentifyOnTheHomeTabAndBackReturns() {
        runComposeUiTest {
            val backInput = setShell(NavigationStyle.Material)

            onNode(hasClickAction() and hasContentDescription("Identify a breed from a photo")).performClick()
            onNode(hasText("Choose photo")).performClick()
            waitForIdle()

            onNode(screenTitle("Identify a breed")).assertExists()
            onNode(tab("Home")).assertIsSelected()

            performPredictiveBack(backInput)

            onNode(screenTitle("Identify a breed")).assertDoesNotExist()
            onNode(homeScreen).assertExists()
        }
    }

    @Test
    fun tappingAFavouriteInProfileOpensItsDetailOnTheProfileTab() {
        runComposeUiTest {
            setShell(NavigationStyle.Material)
            onNode(tab("Profile")).performClick()

            onNode(hasText("Border Collie")).performClick()
            waitForIdle()

            assertDetailTitle("Border Collie")
            onNode(tab("Profile")).assertIsSelected()
        }
    }

    @Test
    fun seeAllFavouritesFromProfileOpensTheFullListOnTheProfileTabAndBackReturns() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                favourites.records.value =
                    listOf(
                        FavouriteRecord("border collie", "Border Collie", addedAtEpochMillis = 1),
                        FavouriteRecord("akita", "Akita", addedAtEpochMillis = 2),
                        FavouriteRecord("beagle", "Beagle", addedAtEpochMillis = 3),
                    )
                val backInput = setShell(style)
                onNode(tab("Profile")).performClick()

                onNode(hasText("Beagle")).assertExists()
                onNode(hasText("Border Collie")).assertDoesNotExist()
                onNode(hasText(if (style == NavigationStyle.Material) "See all (3)" else "See All (3)")).performClick()
                onNode(hasText("Border Collie")).performClick()
                waitForIdle()
                assertDetailTitle("Border Collie")
                onNode(tab("Profile")).assertIsSelected()

                performPredictiveBack(backInput)
                onNode(screenTitle("Favourite breeds")).assertExists()
                onNode(hasText("Border Collie")).assertExists()
                performPredictiveBack(backInput)
                onNode(screenTitle("Profile")).assertExists()
            }
        }
    }

    @Test
    fun supportingDogCharitiesFromProfileOpensACharityOnTheProfileTabAndBackReturns() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                val backInput = setShell(style)
                onNode(tab("Profile")).performClick()

                onNode(verticalList).performScrollToNode(hasText("Support dog charities"))
                onNode(hasText("Support dog charities")).performClick()
                onNode(screenTitle("Dog charities")).assertExists()
                onNode(verticalList).performScrollToNode(hasText("Fetch Club"))
                onNode(hasText("Fetch Club")).performClick()
                onNode(screenTitle("Fetch Club")).assertExists()
                onNode(tab("Profile")).assertIsSelected()

                performPredictiveBack(backInput)
                onNode(screenTitle("Dog charities")).assertExists()
                performPredictiveBack(backInput)
                onNode(screenTitle("Profile")).assertExists()
            }
        }
    }

    @Test
    fun donationsFromProfileShowTheEmptyHistoryWhichLeadsToTheCharitiesAndBackReturns() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                val backInput = setShell(style)
                onNode(tab("Profile")).performClick()

                onNode(verticalList).performScrollToNode(hasText("Donations"))
                onNode(hasText("Donations")).performClick()
                onNode(screenTitle("Donations")).assertExists()
                onNode(hasText("No donations yet.")).assertExists()
                onNode(hasText("See dog charities")).performClick()
                onNode(screenTitle("Dog charities")).assertExists()
                onNode(tab("Profile")).assertIsSelected()

                performPredictiveBack(backInput)
                onNode(screenTitle("Donations")).assertExists()
                performPredictiveBack(backInput)
                onNode(screenTitle("Profile")).assertExists()
            }
        }
    }

    @Test
    fun deviceStateFromProfileShowTheSimulatorWarningOnTheProfileTabAndBackReturns() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                val backInput = setShell(style)
                onNode(tab("Profile")).performClick()

                onNode(verticalList).performScrollToNode(hasText("Device state"))
                onNode(hasText("1 warning")).assertExists()
                onNode(hasText("Device state")).performClick()
                onNode(screenTitle("Device state")).assertExists()
                onNode(hasText("Simulator") and hasText("Warning")).assertExists()
                onNode(hasText("Jailbreak files") and hasText("Not found")).assertExists()
                onNode(tab("Profile")).assertIsSelected()

                performPredictiveBack(backInput)
                onNode(screenTitle("Profile")).assertExists()
            }
        }
    }

    @Test
    fun walletFromProfileUnlocksOnOpenOnTheProfileTabAndBackReturns() {
        NavigationStyle.entries.forEach { style ->
            runComposeUiTest {
                val backInput = setShell(style)
                onNode(tab("Profile")).performClick()

                onNode(verticalList).performScrollToNode(hasText("Wallet"))
                onNode(hasText("Wallet")).performClick()
                onNode(screenTitle("Wallet")).assertExists()
                onNode(hasText("No cards yet")).assertExists()
                onNode(tab("Profile")).assertIsSelected()

                performPredictiveBack(backInput)
                onNode(screenTitle("Profile")).assertExists()
            }
        }
    }

    @Test
    fun addingAndRemovingACardOnMaterialGoesThroughTheFormAndTheCardDetail() {
        runComposeUiTest {
            setShell(NavigationStyle.Material)
            onNode(tab("Profile")).performClick()
            onNode(verticalList).performScrollToNode(hasText("Wallet"))
            onNode(hasText("Wallet")).performClick()

            onNode(hasContentDescription("Add card")).performClick()
            onNode(screenTitle("Add card")).assertExists()
            onNode(hasSetTextAction() and hasText("Card number")).performTextInput("4242 4242 4242 4242")
            onNode(hasSetTextAction() and hasText("Expiry (MM/YY)")).performTextInput("12/40")
            onNode(hasSetTextAction() and hasText("Security code")).performTextInput("123")
            onNode(hasText("Save card") and hasClickAction()).performClick()
            waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("Visa •••• 4242")).fetchSemanticsNodes().isNotEmpty() }

            onNode(screenTitle("Wallet")).assertExists()
            onNode(hasText("Expires 12/40")).assertExists()

            onNode(hasText("Visa •••• 4242")).performClick()
            onNode(screenTitle("Visa •••• 4242")).assertExists()
            onNode(hasText("Remove card") and hasClickAction()).performClick()
            waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("No cards yet")).fetchSemanticsNodes().isNotEmpty() }
            onNode(screenTitle("Wallet")).assertExists()
        }
    }

    @Test
    fun donatingOnMaterialOpensTheSheetUnlocksTheWalletAndLeadsToAddingACard() {
        runComposeUiTest {
            setShell(NavigationStyle.Material)
            onNode(tab("Profile")).performClick()
            onNode(verticalList).performScrollToNode(hasText("Support dog charities"))
            onNode(hasText("Support dog charities")).performClick()
            onNode(verticalList).performScrollToNode(hasText("Fetch Club"))
            onNode(hasText("Fetch Club")).performClick()

            onNode(hasText("Donate") and hasClickAction()).performClick()
            onNode(screenTitle("Donate to Fetch Club")).assertExists()
            onNode(hasText("Unlock wallet to pay with a card")).performClick()
            waitUntil(timeoutMillis = 5_000) { onAllNodes(hasText("Add a card in your wallet")).fetchSemanticsNodes().isNotEmpty() }
            onNode(hasText("Add a card in your wallet")).performClick()

            waitUntil(timeoutMillis = 5_000) { onAllNodes(screenTitle("Wallet")).fetchSemanticsNodes().isNotEmpty() }
            onNode(hasText("No cards yet")).assertExists()
        }
    }
}
