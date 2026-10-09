package com.example.dogapp.feature.explore.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.dogapp.feature.explore.domain.BreedFilters
import com.example.dogapp.feature.explore.domain.BreedInfo
import com.example.dogapp.feature.explore.domain.BreedSize
import com.example.dogapp.feature.explore.domain.FilterCategory
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ExploreScreenTest {
    private val searchField = SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)

    @Test
    fun loadingInformsTheUserDisablesSearchAndHidesTheFilters() =
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
            onNodeWithText("Favourites").assertDoesNotExist()
        }

    @Test
    fun errorShowsTheReasonHidesTheFiltersAndRetries() =
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
            onNodeWithText("Favourites").assertDoesNotExist()
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
                                                imageUrls = emptyList(),
                                                isFavourite = true,
                                            ),
                                            BreedListItem(
                                                "beagle",
                                                "Beagle",
                                                subtitle = null,
                                                imageUrls = emptyList(),
                                                isFavourite = false,
                                            ),
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
            onNodeWithText("Favourites").assertExists()
            onNodeWithText("Size").assertDoesNotExist()
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

    @Test
    fun onlyFiltersWithInfoShowChipsThatOpenOptionSheetsSummariseActiveFiltersAndClearThem() =
        runComposeUiTest {
            val actions = mutableListOf<ExploreAction>()
            val filters = BreedFilters(sizes = setOf(BreedSize.Medium), groups = setOf("Hound"))
            setContent {
                DogAppTheme {
                    ExploreScreen(
                        state =
                            ExploreUiState(
                                filters = filters,
                                availableFilters = setOf(FilterCategory.Size, FilterCategory.Group),
                                groupOptions = listOf("Herding", "Hound"),
                                content =
                                    ExploreContent.Results(
                                        listOf(
                                            BreedListItem(
                                                "beagle",
                                                "Beagle",
                                                subtitle = "Hound",
                                                imageUrls = emptyList(),
                                                isFavourite = false,
                                            ),
                                        ),
                                        hiddenForMissingInfo = mapOf(BreedInfo.Size to 1, BreedInfo.Group to 12),
                                    ),
                            ),
                        onAction = { actions += it },
                        onBreedClick = {},
                    )
                }
            }

            onNodeWithText("1 breed hidden: no size listed").assertExists()
            onNodeWithText("12 breeds hidden: no group listed").assertExists()

            onNodeWithText("Group: Hound").assertExists()
            onNodeWithText("Life span").assertDoesNotExist()
            onNodeWithText("Energy").assertDoesNotExist()
            onNodeWithText("Traits").assertDoesNotExist()
            onNodeWithText("Favourites").performClick()
            onNodeWithText("Clear all").performClick()

            onNodeWithText("Size: Medium").performClick()
            onNodeWithText("Toy · Under 5 kg").assertExists()
            onNodeWithText("Giant · Over 45 kg").performClick()
            onNodeWithText("Medium · 11–25 kg").performClick()

            assertEquals(
                listOf<ExploreAction>(
                    ExploreAction.FiltersChange(filters.copy(favouritesOnly = true)),
                    ExploreAction.FiltersChange(BreedFilters()),
                    ExploreAction.FiltersChange(filters.copy(sizes = setOf(BreedSize.Medium, BreedSize.Giant))),
                    ExploreAction.FiltersChange(filters.copy(sizes = emptySet())),
                ),
                actions,
            )
        }

    @Test
    fun noMatchesWithFiltersSaysSoAndExplainsHiddenBreeds() =
        runComposeUiTest {
            var state by mutableStateOf(
                ExploreUiState(
                    filters = BreedFilters(favouritesOnly = true),
                    content = ExploreContent.NoMatches("", hasFilters = true),
                ),
            )
            setContent { DogAppTheme { ExploreScreen(state = state, onAction = {}, onBreedClick = {}) } }

            onNodeWithText("No breeds match these filters").assertExists()

            state =
                ExploreUiState(
                    query = "collie",
                    filters = BreedFilters(sizes = setOf(BreedSize.Giant)),
                    content = ExploreContent.NoMatches("collie", hasFilters = true, hiddenForMissingInfo = mapOf(BreedInfo.Size to 2)),
                )
            onNodeWithText("No breeds match “collie” with these filters").assertExists()
            onNodeWithText("2 breeds hidden: no size listed").assertExists()
        }
}
