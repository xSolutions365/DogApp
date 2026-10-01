package com.example.dogapp.feature.profile.presentation

import androidx.compose.ui.test.ExperimentalTestApi
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
            setContent { DogAppTheme { ProfileScreen(state = ProfileUiState(favourites = emptyList()), onAction = {}, onBreedClick = {}) } }

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
}
