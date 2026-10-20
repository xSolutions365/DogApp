package com.example.dogapp.feature.donate.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaidWith
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DonationsScreenTest {
    @Test
    fun historySaysHowEachDonationWasPaidAndAnEmptyHistoryLeadsToTheCharities() =
        runComposeUiTest {
            var charitiesOpened = 0
            var backs = 0
            var state by mutableStateOf(DonationsUiState())
            setContent {
                DogAppTheme {
                    DonationsScreen(state = state, onSeeCharitiesClick = { charitiesOpened++ }, onBack = { backs++ })
                }
            }

            onNode(isHeading() and hasText("Donations")).assertExists()
            onNodeWithText("Demo only: no money was taken.").assertExists()
            onNodeWithText("No donations yet.").assertDoesNotExist()

            state = DonationsUiState(donations = emptyList())
            waitForIdle()
            onNodeWithText("No donations yet.").assertExists()
            onNodeWithText("See dog charities").performClick()

            state =
                DonationsUiState(
                    donations =
                        listOf(
                            DonationListItem("g1", "Fetch Club", DonationAmount(750), PaidWith.GooglePay("Visa •••• 1111"), "20 Oct 2026"),
                            DonationListItem("g2", "Wag Street Rescue", DonationAmount(300), PaidWith.GooglePay(null), "19 Oct 2026"),
                            DonationListItem("c1", "Good Boy Vet Fund", DonationAmount(2000), PaidWith.Card("Visa", "4242"), "18 Oct 2026"),
                        ),
                )
            waitForIdle()
            onNodeWithText("No donations yet.").assertDoesNotExist()
            onNodeWithText("Paid with Google Pay (Visa •••• 1111)").assertExists()
            onNodeWithText("£7.50").assertExists()
            onNodeWithText("Paid with Google Pay").assertExists()
            onNodeWithText("Paid with Visa •••• 4242").assertExists()
            onNodeWithText("£20").assertExists()
            onNodeWithText("18 Oct 2026").assertExists()

            assertEquals(1, charitiesOpened)
            assertEquals(0, backs)
        }
}
