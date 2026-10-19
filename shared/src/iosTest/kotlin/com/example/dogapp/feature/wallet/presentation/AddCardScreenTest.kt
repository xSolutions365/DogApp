package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardFieldError
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AddCardScreenTest {
    @Test
    fun screenFormSaysItIsADemoNamesTheBrandAndExplainsEachError() =
        runComposeUiTest {
            val actions = mutableListOf<AddCardAction>()
            var dismissals = 0
            var state by mutableStateOf(AddCardUiState())
            setContent {
                DogAppTheme {
                    AddCardScreen(
                        state = state,
                        presentation = AddCardPresentation.Screen,
                        isScreenCaptured = false,
                        onAction = { actions += it },
                        onDismiss = { dismissals++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Add card")).assertExists()
            onNodeWithText("Demo only: use a test card such as 4242 4242 4242 4242. Only the brand, last 4 digits and expiry are kept.")
                .assertExists()
            onNode(hasSetTextAction() and hasText("Card number")).performTextInput("4")
            onNode(hasText("Save card") and hasClickAction()).performClick()

            state =
                AddCardUiState(
                    number = "4242",
                    brand = CardBrand.Visa,
                    errors = setOf(CardFieldError.InvalidNumber, CardFieldError.ExpiredCard, CardFieldError.InvalidSecurityCode),
                )
            waitForIdle()
            onNodeWithText("Check the card number").assertExists()
            onNodeWithText("This card has expired").assertExists()
            onNodeWithText("Check the security code").assertExists()

            state = state.copy(errors = setOf(CardFieldError.InvalidExpiry))
            waitForIdle()
            onNodeWithText("Visa").assertExists()
            onNodeWithText("Enter the expiry as MM/YY").assertExists()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf(AddCardAction.NumberChange("4"), AddCardAction.Save), actions)
            assertEquals(1, dismissals)
        }

    @Test
    fun sheetHasCancelAndAddAndHidesTheFormWhileRecorded() =
        runComposeUiTest {
            val actions = mutableListOf<AddCardAction>()
            var dismissals = 0
            var isScreenCaptured by mutableStateOf(false)
            setContent {
                DogAppTheme {
                    AddCardScreen(
                        state = AddCardUiState(),
                        presentation = AddCardPresentation.Sheet,
                        isScreenCaptured = isScreenCaptured,
                        onAction = { actions += it },
                        onDismiss = { dismissals++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Add Card")).assertExists()
            onNodeWithText("Save card").assertDoesNotExist()
            onNodeWithText("Add").performClick()
            onNodeWithText("Cancel").performClick()

            isScreenCaptured = true
            waitForIdle()
            onNodeWithText("Your wallet is hidden while the screen is being recorded.").assertExists()
            onNode(hasSetTextAction() and hasText("Card number")).assertDoesNotExist()

            assertEquals(listOf<AddCardAction>(AddCardAction.Save), actions)
            assertEquals(1, dismissals)
        }
}
