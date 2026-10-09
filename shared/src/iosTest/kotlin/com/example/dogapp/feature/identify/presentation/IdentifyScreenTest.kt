package com.example.dogapp.feature.identify.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.identify_error_model
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class IdentifyScreenTest {
    private val photo = byteArrayOf(1)

    @Test
    fun choosingAPhotoStartsFromTheIntroAndIsBlockedWhileIdentifying() =
        runComposeUiTest {
            var chooseClicks = 0
            var backClicks = 0
            var state by mutableStateOf(IdentifyUiState())
            setContent {
                DogAppTheme {
                    IdentifyScreen(
                        state = state,
                        presentation = IdentifyPresentation.Screen,
                        onChoosePhoto = { chooseClicks++ },
                        onBreedClick = {},
                        onDismiss = { backClicks++ },
                    )
                }
            }

            onNodeWithText("Choose a photo of a dog to find out its breed.").assertExists()
            onNode(hasClickAction() and hasText("Choose a photo")).performClick()
            onNodeWithText("Done").assertDoesNotExist()
            onNodeWithContentDescription("Back").performClick()
            assertEquals(1, chooseClicks)
            assertEquals(1, backClicks)

            state = IdentifyUiState(photo = photo, content = IdentifyContent.Identifying)
            onNodeWithText("Looking closely…").assertExists()
            onNodeWithText("Choose another photo").assertIsNotEnabled()

            state = IdentifyUiState(photo = photo, content = IdentifyContent.NoDog)
            onNodeWithText("No dog found in this photo. Try one where the dog is clearly visible.").assertExists()

            state = IdentifyUiState(photo = photo, content = IdentifyContent.Error(Res.string.identify_error_model))
            onNodeWithText("Breed recognition isn't available right now.").assertExists()
        }

    @Test
    fun aConfidentMatchOpensItsBreed() =
        runComposeUiTest {
            val opened = mutableListOf<String>()
            setContent {
                DogAppTheme {
                    IdentifyScreen(
                        presentation = IdentifyPresentation.Screen,
                        state =
                            IdentifyUiState(
                                photo = photo,
                                content =
                                    IdentifyContent.Matches(
                                        isConfident = true,
                                        matches = listOf(BreedMatch("akita", "Akita", 87, emptyList())),
                                    ),
                            ),
                        onChoosePhoto = {},
                        onBreedClick = { opened += it },
                        onDismiss = {},
                    )
                }
            }

            onNodeWithText("Looks like a").assertExists()
            onNodeWithText("87% match").assertExists()
            onNodeWithText("Akita").performClick()

            assertEquals(listOf("akita"), opened)
        }

    @Test
    fun uncertainCandidatesOpenTheirBreedsButBreedsMissingFromTheListAreNameOnly() =
        runComposeUiTest {
            val opened = mutableListOf<String>()
            setContent {
                DogAppTheme {
                    IdentifyScreen(
                        presentation = IdentifyPresentation.Screen,
                        state =
                            IdentifyUiState(
                                photo = photo,
                                content =
                                    IdentifyContent.Matches(
                                        isConfident = false,
                                        matches =
                                            listOf(
                                                BreedMatch("beagle", "Beagle", 40, emptyList()),
                                                BreedMatch(null, "Eskimo dog", 25, emptyList()),
                                                BreedMatch("pug", "Pug", 10, emptyList()),
                                            ),
                                    ),
                            ),
                        onChoosePhoto = {},
                        onBreedClick = { opened += it },
                        onDismiss = {},
                    )
                }
            }

            onNodeWithText("Not quite sure. It could be one of these:").assertExists()
            onNodeWithText("Eskimo dog").assertHasNoClickAction()
            onNodeWithText("25% match").assertExists()
            onNodeWithText("Pug").performClick()

            assertEquals(listOf("pug"), opened)
        }

    @Test
    fun asASheetItIsDismissedWithDoneInsteadOfBack() =
        runComposeUiTest {
            var dismissals = 0
            setContent {
                DogAppTheme {
                    IdentifyScreen(
                        state = IdentifyUiState(),
                        presentation = IdentifyPresentation.Sheet,
                        onChoosePhoto = {},
                        onBreedClick = {},
                        onDismiss = { dismissals++ },
                    )
                }
            }

            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithText("Done").performClick()

            assertEquals(1, dismissals)
        }
}
