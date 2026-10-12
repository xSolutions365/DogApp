package com.example.dogapp.feature.donate.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.testing.testCharity
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CharityScreensTest {
    @Test
    fun charityListSaysItIsADemoAndOpensTheTappedCharity() =
        runComposeUiTest {
            val opened = mutableListOf<String>()
            var backs = 0
            setContent {
                DogAppTheme {
                    CharitiesScreen(
                        state =
                            CharitiesUiState(
                                listOf(testCharity("wag-street", "Wag Street Rescue"), testCharity("fetch-club", "Fetch Club")),
                            ),
                        onCharityClick = { opened += it },
                        onBack = { backs++ },
                    )
                }
            }

            onNodeWithText("Dog charities").assertExists()
            onNodeWithText("Demo only: these charities are made up and no real money is ever taken.").assertExists()
            onNodeWithText("Fetch Club").performClick()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf("fetch-club"), opened)
            assertEquals(1, backs)
        }

    @Test
    fun charityDetailShowsItsStoryOrSaysItIsUnavailable() =
        runComposeUiTest {
            var state: CharityUiState by mutableStateOf(CharityUiState.Loaded(testCharity("fetch-club", "Fetch Club")))
            setContent { DogAppTheme { CharityScreen(state = state, onBack = {}) } }

            onNodeWithText("Fetch Club").assertExists()
            onNodeWithText("Tagline of Fetch Club").assertExists()
            onNodeWithText("About Fetch Club").assertExists()

            state = CharityUiState.NotFound
            waitForIdle()

            onNodeWithText("This charity isn't available.").assertExists()
        }
}
