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
class FavouritesScreenTest {
    @Test
    fun theFullListShowsEveryFavouriteOpensOrRemovesThemAndExplainsWhenEmpty() =
        runComposeUiTest {
            val actions = mutableListOf<ProfileAction>()
            val opened = mutableListOf<String>()
            var backs = 0
            var favourites by mutableStateOf<List<FavouriteListItem>?>(
                listOf(
                    FavouriteListItem.Available("akita", "Akita", subtitle = "Working", imageUrls = emptyList()),
                    FavouriteListItem.Available("beagle", "Beagle", subtitle = "Hound", imageUrls = emptyList()),
                    FavouriteListItem.Unavailable("blenheim spaniel", "Blenheim Spaniel"),
                ),
            )
            setContent {
                DogAppTheme {
                    FavouritesScreen(
                        favourites = favourites,
                        onAction = { actions += it },
                        onBreedClick = { opened += it },
                        onBack = { backs++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Favourite breeds")).assertExists()
            onNodeWithText("Akita").assertExists()
            onNodeWithText("Beagle").performClick()
            onNodeWithContentDescription("Remove Blenheim Spaniel from favourites").performClick()
            onNodeWithContentDescription("Back").performClick()

            favourites = emptyList()
            waitForIdle()
            onNodeWithText("No favourites yet. Tap the heart on a breed to save it here.").assertExists()

            assertEquals(listOf("beagle"), opened)
            assertEquals(listOf<ProfileAction>(ProfileAction.RemoveFavourite("blenheim spaniel")), actions)
            assertEquals(1, backs)
        }
}
