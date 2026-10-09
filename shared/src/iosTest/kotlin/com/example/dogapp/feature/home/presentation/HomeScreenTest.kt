package com.example.dogapp.feature.home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {
    private val revealButton = hasClickAction() and hasText("Reveal today's breed")
    private val identifyButton = hasClickAction() and hasText("Identify a breed from a photo")

    @Test
    fun loadingAndErrorStatesExplainTheFirstSyncAndRetry() =
        runComposeUiTest {
            val actions = mutableListOf<HomeAction>()
            var state by mutableStateOf(HomeUiState(content = HomeContent.Loading))
            setContent { DogAppTheme { HomeScreen(state = state, onAction = { actions += it }, onBreedClick = {}, onIdentifyClick = {}) } }

            onNodeWithText("Fetching all the breed data…").assertExists()
            onNode(identifyButton).assertDoesNotExist()

            state = HomeUiState(content = HomeContent.Error(DataError.Remote.NoConnection.toStringResource()))
            onNodeWithText("We couldn't load the breeds").assertExists()
            onNodeWithText("Try again").performClick()

            assertEquals(listOf<HomeAction>(HomeAction.Retry), actions)
        }

    @Test
    fun revealButtonDrawsAndIsDisabledWhileDrawing() =
        runComposeUiTest {
            val actions = mutableListOf<HomeAction>()
            var identifyClicks = 0
            var state by mutableStateOf(HomeUiState(content = HomeContent.NotRevealed(isDrawing = false)))
            setContent {
                DogAppTheme {
                    HomeScreen(
                        state = state,
                        onAction = { actions += it },
                        onBreedClick = {},
                        onIdentifyClick = { identifyClicks++ },
                    )
                }
            }

            onNodeWithText("Your breed of the day").assertExists()
            onNode(revealButton).performClick()
            assertEquals(listOf<HomeAction>(HomeAction.Reveal), actions)
            onNode(identifyButton).performClick()
            assertEquals(1, identifyClicks)

            state = HomeUiState(content = HomeContent.NotRevealed(isDrawing = true))
            onNode(revealButton).assertIsNotEnabled()
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
                        onAction = {},
                        onBreedClick = { opened += it },
                        onIdentifyClick = {},
                    )
                }
            }

            onNodeWithText("Today's breed").assertExists()
            onNode(revealButton).assertDoesNotExist()
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
                        onAction = {},
                        onBreedClick = {},
                        onIdentifyClick = {},
                    )
                }
            }

            onNodeWithText("No longer available").assertExists()
            onNodeWithText("Blenheim Spaniel").assertHasNoClickAction()
        }
}
