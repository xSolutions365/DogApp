package com.example.dogapp.feature.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
class ProfileScreenTest {
    @Test
    fun emptyFavouritesExplainHowToAddOne() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state = ProfileUiState(favourites = emptyList()),
                        onAction = {},
                        onBreedClick = {},
                        onSupportCharitiesClick = {},
                        onDonationsClick = {},
                        onWalletClick = {},
                        onDeviceStateClick = {},
                        layout = ProfileLayout.Material,
                        onSeeAllFavouritesClick = {},
                        onSeeAllDrawsClick = {},
                    )
                }
            }

            onNodeWithText("Favourite breeds").assertExists()
            onNodeWithText("No favourites yet. Tap the heart on a breed to save it here.").assertExists()
        }

    @Test
    fun availableFavouritesOpenAndUnavailableOnesCanBeRemoved() =
        runComposeUiTest {
            val actions = mutableListOf<ProfileAction>()
            val opened = mutableListOf<String>()
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state =
                            ProfileUiState(
                                favourites =
                                    listOf(
                                        FavouriteListItem.Available("akita", "Akita", subtitle = "Working", imageUrls = emptyList()),
                                        FavouriteListItem.Unavailable("blenheim spaniel", "Blenheim Spaniel"),
                                    ),
                            ),
                        onAction = { actions += it },
                        onBreedClick = { opened += it },
                        onSupportCharitiesClick = {},
                        onDonationsClick = {},
                        onWalletClick = {},
                        onDeviceStateClick = {},
                        layout = ProfileLayout.Material,
                        onSeeAllFavouritesClick = {},
                        onSeeAllDrawsClick = {},
                    )
                }
            }

            onNodeWithText("Working").assertExists()
            onNodeWithText("No longer available").assertExists()
            onNodeWithText("Akita").performClick()
            onNodeWithContentDescription("Remove Blenheim Spaniel from favourites").performClick()

            assertEquals(listOf("akita"), opened)
            assertEquals(listOf<ProfileAction>(ProfileAction.RemoveFavourite("blenheim spaniel")), actions)
        }

    @Test
    fun drawHistoryShowsEachDayAndOpensOnlyBreedsStillInTheCatalogue() =
        runComposeUiTest {
            val opened = mutableListOf<String>()
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state =
                            ProfileUiState(
                                favourites = emptyList(),
                                draws =
                                    listOf(
                                        DrawHistoryItem(
                                            "2026-09-30",
                                            "30 Sep 2026",
                                            "akita",
                                            "Akita",
                                            emptyList(),
                                            isAvailable = true,
                                        ),
                                        DrawHistoryItem(
                                            "2026-09-28",
                                            "28 Sep 2026",
                                            "blenheim spaniel",
                                            "Blenheim Spaniel",
                                            emptyList(),
                                            isAvailable = false,
                                        ),
                                    ),
                            ),
                        onAction = {},
                        onBreedClick = { opened += it },
                        onSupportCharitiesClick = {},
                        onDonationsClick = {},
                        onWalletClick = {},
                        onDeviceStateClick = {},
                        layout = ProfileLayout.Material,
                        onSeeAllFavouritesClick = {},
                        onSeeAllDrawsClick = {},
                    )
                }
            }

            onNodeWithText("Previous breeds of the day").assertExists()
            onNodeWithText("30 Sep 2026").assertExists()
            onNodeWithText("28 Sep 2026 · No longer available").assertExists()
            onNodeWithText("Blenheim Spaniel").performClick()
            onNodeWithText("Akita").performClick()

            assertEquals(listOf("akita"), opened)
        }

    @Test
    fun eachListShowsAtMostTwoRowsThenSeeAllWithTheCountInThePlatformCasing() =
        runComposeUiTest {
            var seeAllFavourites = 0
            var seeAllDraws = 0
            var layout by mutableStateOf(ProfileLayout.Material)
            var state by mutableStateOf(ProfileUiState(favourites = favourites(1), draws = draws(2)))
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state = state,
                        layout = layout,
                        onAction = {},
                        onBreedClick = {},
                        onSeeAllFavouritesClick = { seeAllFavourites++ },
                        onSeeAllDrawsClick = { seeAllDraws++ },
                        onSupportCharitiesClick = {},
                        onDonationsClick = {},
                        onWalletClick = {},
                        onDeviceStateClick = {},
                    )
                }
            }

            onNodeWithText("Breed 1").assertExists()
            onNodeWithText("Day 1").assertExists()
            onNodeWithText("Day 2").assertExists()
            onNodeWithText("See all", substring = true).assertDoesNotExist()

            state = ProfileUiState(favourites = favourites(5), draws = draws(3))
            waitForIdle()
            onNodeWithText("Breed 2").assertExists()
            onNodeWithText("Breed 3").assertDoesNotExist()
            onNodeWithText("Day 3").assertDoesNotExist()
            onNodeWithText("See all (5)").performClick()
            onNodeWithText("See all (3)").performClick()

            layout = ProfileLayout.Cupertino
            waitForIdle()
            onNodeWithText("See All (5)").assertExists()
            onNodeWithText("See All (3)").assertExists()

            assertEquals(1, seeAllFavourites)
            assertEquals(1, seeAllDraws)
        }

    @Test
    fun emptyDrawHistoryExplainsWhereDrawsComeFrom() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state = ProfileUiState(favourites = emptyList(), draws = emptyList()),
                        onAction = {},
                        onBreedClick = {},
                        onSupportCharitiesClick = {},
                        onDonationsClick = {},
                        onWalletClick = {},
                        onDeviceStateClick = {},
                        layout = ProfileLayout.Material,
                        onSeeAllFavouritesClick = {},
                        onSeeAllDrawsClick = {},
                    )
                }
            }

            onNodeWithText("Each breed of the day you reveal will appear here the following day.").assertExists()
        }

    @Test
    fun feedMeOpensTheDogCharitiesTheDonationsAndTheWalletAndAboutAtTheBottomOpensTheDeviceState() =
        runComposeUiTest {
            var charitiesOpened = 0
            var donationsOpened = 0
            var walletOpened = 0
            var deviceStateOpened = 0
            var state by mutableStateOf(ProfileUiState(favourites = emptyList(), draws = emptyList(), deviceStateWarnings = 2))
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state = state,
                        onAction = {},
                        onBreedClick = {},
                        onSupportCharitiesClick = { charitiesOpened++ },
                        onDonationsClick = { donationsOpened++ },
                        onWalletClick = { walletOpened++ },
                        onDeviceStateClick = { deviceStateOpened++ },
                        layout = ProfileLayout.Material,
                        onSeeAllFavouritesClick = {},
                        onSeeAllDrawsClick = {},
                    )
                }
            }

            onNode(isHeading() and hasText("Feed me")).assertExists()
            onNodeWithText("Support dog charities").performClick()
            onNodeWithText("Donations").performClick()
            onNodeWithText("Wallet").performClick()
            onNode(isHeading() and hasText("About")).assertExists()
            onNodeWithText("2 warnings").assertExists()
            onNodeWithText("Device state").performClick()
            val rowTops =
                listOf(
                    "Feed me",
                    "Support dog charities",
                    "Donations",
                    "Wallet",
                    "About",
                    "Device state",
                ).map { onNodeWithText(it).fetchSemanticsNode().boundsInRoot.top }
            assertEquals(rowTops.sorted(), rowTops)

            state = state.copy(deviceStateWarnings = 1)
            waitForIdle()
            onNodeWithText("1 warning").assertExists()
            state = state.copy(deviceStateWarnings = 0)
            waitForIdle()
            onNodeWithText("No warnings").assertExists()

            assertEquals(1, charitiesOpened)
            assertEquals(1, donationsOpened)
            assertEquals(1, walletOpened)
            assertEquals(1, deviceStateOpened)
        }

    private fun favourites(count: Int) =
        (1..count).map { FavouriteListItem.Available("breed-$it", "Breed $it", subtitle = null, imageUrls = emptyList()) }

    private fun draws(count: Int) =
        (1..count).map { DrawHistoryItem("2026-09-${10 + it}", "Day $it", "draw-$it", "Drawn $it", emptyList(), isAvailable = true) }
}
