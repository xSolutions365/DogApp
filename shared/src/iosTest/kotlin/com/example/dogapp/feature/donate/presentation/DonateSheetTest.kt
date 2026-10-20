package com.example.dogapp.feature.donate.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaidWith
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.testing.testPaymentCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

@OptIn(ExperimentalTestApi::class)
class DonateSheetTest {
    private val visa = testPaymentCard("4242")
    private val amex = testPaymentCard("0005", brandName = "American Express")
    private val googlePay =
        object : GooglePayLauncher {
            override fun launch(amount: DonationAmount) = Unit

            @Composable
            override fun PayButton(
                onClick: () -> Unit,
                modifier: Modifier,
            ) {
                TextButton(onClick = onClick, modifier = modifier) { Text("Google Pay") }
            }
        }

    @Test
    fun bottomSheetOffersAmountChipsCardsAndExplainsEachWayToPay() =
        runComposeUiTest {
            val actions = mutableListOf<DonateAction>()
            var walletOpened = 0
            var state by mutableStateOf(DonateUiState(charityName = "Fetch Club"))
            setContent {
                DogAppTheme {
                    DonateSheet(
                        state = state,
                        presentation = DonatePresentation.BottomSheet,
                        googlePay = googlePay,
                        onAction = { actions += it },
                        onOpenWallet = { walletOpened++ },
                        onDismiss = {},
                    )
                }
            }

            onNode(isHeading() and hasText("Donate to Fetch Club")).assertExists()
            onNodeWithText("£5").assertIsSelected()
            onNodeWithText("£20").performClick()
            onNodeWithText("Unlock wallet to pay with a card").performClick()

            state = state.copy(cards = emptyList())
            waitForIdle()
            onNodeWithText("Add a card in your wallet").performClick()

            state =
                state.copy(
                    cards = listOf(visa, amex),
                    selectedCardId = visa.id,
                    selectedPreset = null,
                    otherAmount = "7.5",
                    error = PaymentError.Declined,
                )
            waitForIdle()
            onNode(hasSetTextAction() and hasText("Other amount")).assertExists()
            onNodeWithText("Visa •••• 4242").assertExists()
            onNodeWithText("American Express •••• 0005").performClick()
            onNodeWithText("The card was declined. Try another card.").assertExists()
            onNodeWithText("Donate £7.50").performClick()
            onNodeWithText("Google Pay").performClick()

            assertEquals(
                listOf(
                    DonateAction.SelectPreset(DonationAmount(2000)),
                    DonateAction.UnlockWallet,
                    DonateAction.SelectCard(amex.id),
                    DonateAction.DonateWithCard,
                    DonateAction.StartGooglePay,
                ),
                actions,
            )
            assertEquals(1, walletOpened)
        }

    @Test
    fun pageSheetUsesCheckmarkRowsAndThanksInsideTheSheet() =
        runComposeUiTest {
            val actions = mutableListOf<DonateAction>()
            var dismissals = 0
            var state by mutableStateOf(DonateUiState(charityName = "Fetch Club", cards = listOf(visa), selectedCardId = visa.id))
            setContent {
                DogAppTheme {
                    DonateSheet(
                        state = state,
                        presentation = DonatePresentation.Sheet,
                        googlePay = null,
                        onAction = { actions += it },
                        onOpenWallet = {},
                        onDismiss = { dismissals++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Donate")).assertExists()
            onNodeWithText("Fetch Club").assertExists()
            onNodeWithText("£5").assertIsSelected()
            onNodeWithText("£10").performClick()
            onNodeWithText("Donate £5").performClick()
            onNodeWithText("Cancel").performClick()

            state =
                state.copy(
                    completed =
                        Donation(
                            id = "d",
                            charityId = "fetch-club",
                            charityName = "Fetch Club",
                            amount = DonationAmount(500),
                            paidWith = PaidWith.Card("Visa", "4242"),
                            donatedAt = Instant.parse("2026-10-19T12:00:00Z"),
                        ),
                )
            waitForIdle()
            onNode(isHeading() and hasText("Thank you!")).assertExists()
            onNodeWithText("You donated £5 to Fetch Club. Demo only: no money was taken.").assertExists()
            onNodeWithText("Done").performClick()

            assertEquals(listOf(DonateAction.SelectPreset(DonationAmount(1000)), DonateAction.DonateWithCard), actions)
            assertEquals(2, dismissals)
        }
}
