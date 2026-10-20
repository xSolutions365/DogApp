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
                    )
                }
            }

            onNodeWithText("Each breed of the day you reveal will appear here the following day.").assertExists()
        }

    @Test
    fun feedMeSectionAtTheBottomOpensTheDogCharitiesTheDonationsAndTheWallet() =
        runComposeUiTest {
            var charitiesOpened = 0
            var donationsOpened = 0
            var walletOpened = 0
            setContent {
                DogAppTheme {
                    ProfileScreen(
                        state = ProfileUiState(favourites = emptyList(), draws = emptyList()),
                        onAction = {},
                        onBreedClick = {},
                        onSupportCharitiesClick = { charitiesOpened++ },
                        onDonationsClick = { donationsOpened++ },
                        onWalletClick = { walletOpened++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Feed me")).assertExists()
            onNodeWithText("Support dog charities").performClick()
            onNodeWithText("Donations").performClick()
            onNodeWithText("Wallet").performClick()
            val rowTops =
                listOf(
                    "Support dog charities",
                    "Donations",
                    "Wallet",
                ).map { onNodeWithText(it).fetchSemanticsNode().boundsInRoot.top }
            assertEquals(rowTops.sorted(), rowTops)

            assertEquals(1, charitiesOpened)
            assertEquals(1, donationsOpened)
            assertEquals(1, walletOpened)
        }
}
