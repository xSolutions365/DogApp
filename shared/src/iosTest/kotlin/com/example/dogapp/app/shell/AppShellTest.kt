package com.example.dogapp.app.shell

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.example.dogapp.app.di.breedModule
import com.example.dogapp.app.di.favouriteModule
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import com.example.dogapp.core.domain.favourite.FavouriteRecord
import com.example.dogapp.core.domain.favourite.FavouritesRepository
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.breeddetail.di.breedDetailModule
import com.example.dogapp.feature.explore.di.exploreModule
import com.example.dogapp.feature.profile.di.profileModule
import com.example.dogapp.testing.FakeFavouritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppShellTest {
    private val isTab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) = isTab and hasText(label)

    private fun screenTitle(label: String) = isHeading() and hasText(label)

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

    private val favourites = FakeFavouritesRepository(listOf(FavouriteRecord("border collie", "Border Collie", addedAtEpochMillis = 1)))

    private val emptyBreedRepository =
        object : BreedRepository {
            override suspend fun syncBreeds() = BreedSyncResult(succeededSources = emptySet(), failures = emptyMap())

            override fun observeBreeds() = MutableStateFlow(listOf(borderCollie))

            override suspend fun fetchGallery(breed: Breed) = Outcome.Success(emptyList<String>())
        }

    private fun ComposeUiTest.setShell(style: NavigationStyle): DirectNavigationEventInput {
        val backInput = DirectNavigationEventInput()
        setContent {
            val dispatcher = requireNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
            remember(dispatcher) { dispatcher.addInput(backInput) }
            KoinApplication(
                configuration =
                    koinConfiguration {
                        modules(
                            breedModule,
                            favouriteModule,
                            exploreModule,
                            breedDetailModule,
                            profileModule,
                            module {
                                single<BreedRepository> { emptyBreedRepository }
                                single<FavouritesRepository> { favourites }
                            },
                        )
                    },
            ) {
                DogAppTheme { AppShell(navigationStyle = style) }
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
                onNode(screenTitle("Home")).assertExists()

                onNode(tab("Explore")).performClick()
                onNode(tab("Explore")).assertIsSelected()
                onNode(tab("Home")).assertIsNotSelected()
                onNode(exploreSearch).assertExists()
                onNode(screenTitle("Home")).assertDoesNotExist()

                onNode(tab("Profile")).performClick()
                onNode(tab("Profile")).assertIsSelected()
                onNode(screenTitle("Profile")).assertExists()

                onNode(tab("Home")).performClick()
                onNode(tab("Home")).assertIsSelected()
                onNode(screenTitle("Home")).assertExists()
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
            onNode(screenTitle("Home")).assertExists()
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
}
