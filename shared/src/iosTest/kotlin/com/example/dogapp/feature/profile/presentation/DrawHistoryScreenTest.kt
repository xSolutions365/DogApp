package com.example.dogapp.feature.profile.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DrawHistoryScreenTest {
    @Test
    fun theFullHistoryShowsEveryDayAndOpensOnlyBreedsStillInTheCatalogue() =
        runComposeUiTest {
            val opened = mutableListOf<String>()
            var backs = 0
            setContent {
                DogAppTheme {
                    DrawHistoryScreen(
                        draws =
                            listOf(
                                DrawHistoryItem("2026-09-30", "30 Sep 2026", "akita", "Akita", emptyList(), isAvailable = true),
                                DrawHistoryItem("2026-09-29", "29 Sep 2026", "beagle", "Beagle", emptyList(), isAvailable = true),
                                DrawHistoryItem(
                                    "2026-09-28",
                                    "28 Sep 2026",
                                    "blenheim spaniel",
                                    "Blenheim Spaniel",
                                    emptyList(),
                                    isAvailable = false,
                                ),
                            ),
                        onBreedClick = { opened += it },
                        onBack = { backs++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Previous breeds of the day")).assertExists()
            onNodeWithText("28 Sep 2026 · No longer available").assertExists()
            onNodeWithText("Blenheim Spaniel").performClick()
            onNodeWithText("Beagle").performClick()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf("beagle"), opened)
            assertEquals(1, backs)
        }

    @Test
    fun anEmptyHistoryExplainsWhereDrawsComeFrom() =
        runComposeUiTest {
            setContent {
                DogAppTheme { DrawHistoryScreen(draws = emptyList(), onBreedClick = {}, onBack = {}) }
            }

            onNodeWithText("Each breed of the day you reveal will appear here the following day.").assertExists()
        }
}
