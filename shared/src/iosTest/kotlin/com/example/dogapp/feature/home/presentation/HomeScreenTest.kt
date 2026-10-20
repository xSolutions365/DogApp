package com.example.dogapp.feature.home.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {
    private val revealCard = hasClickAction() and hasText("Reveal today's breed")
    private val identifyFab = hasClickAction() and hasContentDescription("Identify a breed from a photo")
    private val identifyBarButton = hasClickAction() and hasText("Identify")

    private fun identifyButton(layout: HomeLayout) =
        when (layout) {
            HomeLayout.Material -> identifyFab
            HomeLayout.Cupertino -> identifyBarButton
        }

    @Test
    fun loadingAndErrorStatesExplainTheFirstSyncAndRetry() =
        runComposeUiTest {
            val actions = mutableListOf<HomeAction>()
            var state by mutableStateOf(HomeUiState(content = HomeContent.Loading))
            setContent {
                DogAppTheme {
                    HomeScreen(
                        state = state,
                        layout = HomeLayout.Material,
                        onAction = { actions += it },
                        onBreedClick = {},
                        onTakePhotoClick = {},
                        onChoosePhotoClick = {},
                        identifyMenuButton = { IdentifyStandIn {} },
                    )
                }
            }

            onNodeWithText("Fetching all the breed data…").assertExists()
            onNode(identifyFab).assertDoesNotExist()
            onNode(identifyBarButton).assertDoesNotExist()

            state = HomeUiState(content = HomeContent.Error(DataError.Remote.NoConnection.toStringResource()))
            onNodeWithText("We couldn't load the breeds").assertExists()
            onNodeWithText("Try again").performClick()

            assertEquals(listOf<HomeAction>(HomeAction.Retry), actions)
        }

    @Test
    fun revealCardDrawsAndEachLayoutOffersIdentifyInItsOwnWay() {
        HomeLayout.entries.forEach { layout ->
            runComposeUiTest {
                val actions = mutableListOf<HomeAction>()
                var takePhotoClicks = 0
                var menuButtonClicks = 0
                var choosePhotoClicks = 0
                var state by mutableStateOf(HomeUiState(content = HomeContent.NotRevealed(isDrawing = false)))
                setContent {
                    DogAppTheme {
                        HomeScreen(
                            state = state,
                            layout = layout,
                            onAction = { actions += it },
                            onBreedClick = {},
                            onTakePhotoClick = { takePhotoClicks++ },
                            onChoosePhotoClick = { choosePhotoClicks++ },
                            identifyMenuButton = { IdentifyStandIn { menuButtonClicks++ } },
                        )
                    }
                }

                onNodeWithText("Your breed of the day").assertExists()
                onNode(revealCard).performClick()
                assertEquals(listOf<HomeAction>(HomeAction.Reveal), actions)

                onNode(identifyButton(HomeLayout.entries.single { it != layout })).assertDoesNotExist()
                val identifyTop = onNode(identifyButton(layout)).getUnclippedBoundsInRoot().top
                val cardTop = onNode(revealCard).getUnclippedBoundsInRoot().top
                when (layout) {
                    HomeLayout.Material -> assertTrue(identifyTop > cardTop, "the FAB sits below the card")
                    HomeLayout.Cupertino -> assertTrue(identifyTop < cardTop, "the toolbar button sits above the card")
                }
                when (layout) {
                    HomeLayout.Material -> {
                        onNodeWithText("Take photo").assertDoesNotExist()
                        onNode(identifyButton(layout)).performClick()
                        onNodeWithText("Take photo").performClick()
                        onNodeWithText("Take photo").assertDoesNotExist()
                        onNode(identifyButton(layout)).performClick()
                        onNode(hasContentDescription("Close the identify menu")).assertExists()
                        onNodeWithText("Choose photo").performClick()
                        assertEquals(1, takePhotoClicks)
                        assertEquals(1, choosePhotoClicks)
                    }

                    HomeLayout.Cupertino -> {
                        onNode(identifyButton(layout)).performClick()
                        assertEquals(1, menuButtonClicks)
                        assertEquals(0, takePhotoClicks + choosePhotoClicks)
                    }
                }

                state = HomeUiState(content = HomeContent.NotRevealed(isDrawing = true))
                onNode(revealCard).assertIsNotEnabled()
            }
        }
    }

    @Test
    fun revealedBreedOpensItsDetailAndPromisesAnotherDrawTomorrow() =
        runComposeUiTest {
            val opened = mutableListOf<String>()
            setContent {
                DogAppTheme {
                    HomeScreen(
                        state =
                            HomeUiState(
                                content =
                                    HomeContent.Revealed(
                                        DrawnBreed.Available("akita", "Akita", subtitle = "Working", imageUrls = emptyList()),
                                    ),
                            ),
                        layout = HomeLayout.Material,
                        onAction = {},
                        onBreedClick = { opened += it },
                        onTakePhotoClick = {},
                        onChoosePhotoClick = {},
                        identifyMenuButton = { IdentifyStandIn {} },
                    )
                }
            }

            onNodeWithText("Today's breed").assertExists()
            onNode(revealCard).assertDoesNotExist()
            onNodeWithText("Working", substring = true).assertExists()
            onNodeWithText("One draw per day. Come back tomorrow for another breed.").assertExists()
            onNodeWithText("Akita", substring = true).performClick()

            assertEquals(listOf("akita"), opened)
        }

    @Test
    fun revealedBreedThatLeftTheCatalogueIsShownByNameAndCannotBeOpened() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    HomeScreen(
                        state = HomeUiState(content = HomeContent.Revealed(DrawnBreed.Unavailable("blenheim spaniel", "Blenheim Spaniel"))),
                        layout = HomeLayout.Material,
                        onAction = {},
                        onBreedClick = {},
                        onTakePhotoClick = {},
                        onChoosePhotoClick = {},
                        identifyMenuButton = { IdentifyStandIn {} },
                    )
                }
            }

            onNodeWithText("No longer available").assertExists()
            onNodeWithText("Blenheim Spaniel").assertHasNoClickAction()
        }

    @Composable
    private fun IdentifyStandIn(onClick: () -> Unit) {
        TextButton(onClick = onClick) { Text("Identify") }
    }
}
