package com.example.dogapp.feature.wallet.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.SavedCard
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CardDetailScreenTest {
    @Test
    fun cardDetailShowsTheCardAndOffersRemoval() =
        runComposeUiTest {
            val actions = mutableListOf<CardDetailAction>()
            var backs = 0
            setContent {
                DogAppTheme {
                    CardDetailScreen(
                        state =
                            CardDetailUiState(
                                SavedCard(id = "card-1", brand = CardBrand.Amex, last4 = "0005", expiry = CardExpiry(3, 2031)),
                            ),
                        onAction = { actions += it },
                        onBack = { backs++ },
                    )
                }
            }

            onNode(isHeading() and hasText("American Express •••• 0005")).assertExists()
            onNodeWithText("Expires 03/31").assertExists()
            onNodeWithText("Remove card").performClick()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf<CardDetailAction>(CardDetailAction.Remove), actions)
            assertEquals(1, backs)
        }
}
