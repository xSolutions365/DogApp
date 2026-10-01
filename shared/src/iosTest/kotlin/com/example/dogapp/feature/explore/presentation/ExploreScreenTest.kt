package com.example.dogapp.feature.explore.presentation

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ExploreScreenTest {
    private val searchField = SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)

    @Test
    fun loadingInformsTheUserAndDisablesSearch() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    ExploreScreen(
                        state = ExploreUiState(content = ExploreContent.Loading),
                        onAction = {},
                        onBreedClick = {},
                    )
                }
            }

            onNodeWithText("Fetching all the breed data…").assertExists()
            onNode(searchField).assertIsNotEnabled()
        }

    @Test
    fun errorShowsTheReasonAndRetries() =
        runComposeUiTest {
            val actions = mutableListOf<ExploreAction>()
            setContent {
                DogAppTheme {
                    ExploreScreen(
                        state = ExploreUiState(content = ExploreContent.Error(DataError.Remote.NoConnection.toStringResource())),
                        onAction = { actions += it },
                        onBreedClick = {},
                    )
                }
            }

            onNodeWithText("We couldn't load the breeds").assertExists()
            onNodeWithText("No internet connection. Check your connection and try again.").assertExists()
            onNode(searchField).assertIsNotEnabled()
            onNodeWithText("Try again").performClick()

            assertEquals(listOf<ExploreAction>(ExploreAction.Retry), actions)
        }

    @Test
    fun resultsListBreedsWithAPassiveFavouriteMarkAndTypingSendsQueryChanges() =
        runComposeUiTest {
            val actions = mutableListOf<ExploreAction>()
            setContent {
                DogAppTheme {
                    ExploreScreen(
                        state =
                            ExploreUiState(
                                content =
                                    ExploreContent.Results(
                                        listOf(
                                            BreedListItem(
                                                "border collie",
                                                "Border Collie",
                                                subtitle = "Herding",
                                                imageUrl = null,
                                                isFavourite = true,
                                            ),
                                            BreedListItem("beagle", "Beagle", subtitle = null, imageUrl = null, isFavourite = false),
                                        ),
                                    ),
                            ),
                        onAction = { actions += it },
                        onBreedClick = {},
                    )
                }
            }

            onNodeWithText("Border Collie").assertExists()
            onNodeWithText("Herding").assertExists()
            onNodeWithText("Beagle").assertExists()
            onAllNodes(hasContentDescription("Favourite"), useUnmergedTree = true).assertCountEquals(1)
            onNode(hasContentDescription("Favourite"), useUnmergedTree = true).assertHasNoClickAction()
            onNode(searchField).assertIsEnabled().performTextInput("col")

            assertEquals(listOf<ExploreAction>(ExploreAction.QueryChange("col")), actions)
        }

    @Test
    fun noMatchesNamesTheQuery() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    ExploreScreen(
                        state = ExploreUiState(query = "poodle", content = ExploreContent.NoMatches("poodle")),
                        onAction = {},
                        onBreedClick = {},
                    )
                }
            }

            onNodeWithText("No breeds match “poodle”").assertExists()
            onNode(searchField).assertIsEnabled()
        }
}
